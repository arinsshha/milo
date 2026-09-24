package com.arin.pocketcompanion.data.repository

import com.arin.pocketcompanion.data.local.ExpenseDao
import com.arin.pocketcompanion.data.local.ExpenseEntity
import com.arin.pocketcompanion.data.local.MemoryDao
import com.arin.pocketcompanion.data.local.MemoryEntity
import com.arin.pocketcompanion.data.local.ReminderDao
import com.arin.pocketcompanion.data.local.ReminderEntity
import com.arin.pocketcompanion.domain.IntentDto
import com.arin.pocketcompanion.domain.IntentTypes
import com.arin.pocketcompanion.domain.MemoryKind
import com.arin.pocketcompanion.domain.Priority
import com.arin.pocketcompanion.domain.RRule
import com.arin.pocketcompanion.domain.ReminderScheduler
import com.arin.pocketcompanion.domain.ReminderStatus
import com.arin.pocketcompanion.domain.Source
import com.arin.pocketcompanion.common.Format
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

enum class ActionKind { REMINDER, MEMORY, EXPENSE, FORGOT }

/** What actually happened, shown as a card in chat. [undoId] lets the user reverse it. */
data class ActionResult(
    val kind: ActionKind,
    val label: String,
    val title: String,
    val detail: String,
    val undoId: String?,
)

sealed interface Outcome {
    data class Done(val result: ActionResult) : Outcome
    data class NeedsConfirmation(val intent: IntentDto, val question: String) : Outcome
    data class Skipped(val reason: String) : Outcome
}

/**
 * The deterministic half of Milo. The AI proposes intents; this class re-checks them
 * and is the only thing allowed to write to the database or schedule alarms.
 */
@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
class IntentExecutor(
    private val memoryDao: MemoryDao,
    private val reminderDao: ReminderDao,
    private val expenseDao: ExpenseDao,
    private val scheduler: ReminderScheduler,
    private val zone: () -> TimeZone = { TimeZone.currentSystemDefault() },
    private val now: () -> Long = { Clock.System.now().toEpochMilliseconds() },
    private val newId: () -> String = { Uuid.random().toString() },
) {

    suspend fun execute(intent: IntentDto, confirmed: Boolean = false): Outcome {
        if (intent.needsConfirmation && !confirmed) {
            return Outcome.NeedsConfirmation(intent, confirmationQuestion(intent))
        }
        return when (intent.type) {
            IntentTypes.CREATE_REMINDER -> createReminder(intent)
            IntentTypes.SAVE_MEMORY -> saveMemory(intent)
            IntentTypes.LOG_EXPENSE -> logExpense(intent)
            IntentTypes.FORGET_MEMORY -> forgetMemory(intent)
            else -> Outcome.Skipped("Nothing to do for ${intent.type}")
        }
    }

    suspend fun undo(result: ActionResult) {
        val id = result.undoId ?: return
        when (result.kind) {
            ActionKind.REMINDER -> { scheduler.cancel(id); reminderDao.delete(id) }
            ActionKind.MEMORY -> memoryDao.delete(id)
            ActionKind.EXPENSE -> expenseDao.delete(id)
            ActionKind.FORGOT -> Unit // forgetting is final by design
        }
    }

    private suspend fun createReminder(intent: IntentDto): Outcome {
        val title = intent.title?.takeIf { it.isNotBlank() } ?: return Outcome.Skipped("Reminder had no title")
        val triggerAt = intent.triggerAt?.let { runCatching { Instant.parse(it).toEpochMilliseconds() }.getOrNull() }
            ?: return Outcome.Skipped("No time given; Milo's reply asks for one")
        if (triggerAt < now() - 60_000) return Outcome.Skipped("That time has already passed")
        val rrule = intent.rrule?.takeIf { RRule.isSupported(it) }
        val reminder = ReminderEntity(
            id = newId(),
            title = title,
            triggerAt = triggerAt,
            rrule = rrule,
            priority = enumOr(intent.priority, Priority.NORMAL),
            memoryId = null,
            status = ReminderStatus.ACTIVE,
        )
        reminderDao.upsert(reminder)
        scheduler.schedule(reminder.id, reminder.title, reminder.triggerAt, reminder.rrule)

        val repeat = rrule?.let { " · repeats " + it.substringAfter("FREQ=").substringBefore(';').lowercase() } ?: ""
        val important = if (reminder.priority == Priority.IMPORTANT) " · Important" else ""
        return Outcome.Done(
            ActionResult(ActionKind.REMINDER, "Reminder set", title, Format.whenLabel(triggerAt, now(), zone()) + repeat + important, reminder.id)
        )
    }

    private suspend fun saveMemory(intent: IntentDto): Outcome {
        val title = intent.title?.takeIf { it.isNotBlank() } ?: return Outcome.Skipped("Memory had no title")
        val date = intent.date?.let { runCatching { LocalDate.parse(it).atStartOfDayIn(zone()).toEpochMilliseconds() }.getOrNull() }
        val expiresAt = intent.expiresInHours?.takeIf { it > 0 }?.let { now() + it * 3_600_000L }
        val memory = MemoryEntity(
            id = newId(),
            kind = if (expiresAt != null) MemoryKind.TEMPORARY else enumOr(intent.memoryKind, if (date != null) MemoryKind.DATE else MemoryKind.FACT),
            title = title,
            detail = intent.detail,
            date = date,
            expiresAt = expiresAt,
            source = Source.USER_SAID,
            createdAt = now(),
        )
        memoryDao.upsert(memory)
        val detail = listOfNotNull(
            date?.let { Format.dayLabel(it, zone()) },
            expiresAt?.let { "Forgets itself in ${intent.expiresInHours}h" },
            intent.detail,
        ).joinToString(" · ").ifEmpty { "Saved" }
        return Outcome.Done(ActionResult(ActionKind.MEMORY, "Saved to memory", title, detail, memory.id))
    }

    private suspend fun logExpense(intent: IntentDto): Outcome {
        val paise = intent.amountPaise?.takeIf { it > 0 } ?: return Outcome.Skipped("No amount; Milo's reply asks for one")
        val expense = ExpenseEntity(
            id = newId(),
            amountPaise = paise,
            category = intent.category?.takeIf { it.isNotBlank() } ?: "Other",
            note = intent.note,
            spentAt = now(),
        )
        expenseDao.insert(expense)
        return Outcome.Done(
            ActionResult(ActionKind.EXPENSE, "Expense logged", Format.rupees(paise), listOfNotNull(expense.category, expense.note).joinToString(" · "), expense.id)
        )
    }

    private suspend fun forgetMemory(intent: IntentDto): Outcome {
        val title = intent.title ?: return Outcome.Skipped("Nothing named to forget")
        val match = memoryDao.findByTitle(title) ?: return Outcome.Skipped("I don't have anything called \"$title\"")
        if (!intent.needsConfirmation) {
            // Forgetting can't be undone, so always ask once.
            return Outcome.NeedsConfirmation(intent.copy(needsConfirmation = true), "Forget \"${match.title}\" for good?")
        }
        memoryDao.delete(match.id)
        return Outcome.Done(ActionResult(ActionKind.FORGOT, "Forgotten", match.title, "Gone for good", null))
    }

    private fun confirmationQuestion(intent: IntentDto): String = when (intent.type) {
        IntentTypes.CREATE_REMINDER -> "Set a reminder for \"${intent.title}\"${intent.triggerAt?.let { " at " + Format.isoToLabel(it, now(), zone()) } ?: ""}?"
        IntentTypes.SAVE_MEMORY -> "Should I remember \"${intent.title}\"?"
        IntentTypes.LOG_EXPENSE -> "Log ${intent.amountPaise?.let { Format.rupees(it) } ?: "this"} under ${intent.category ?: "Other"}?"
        IntentTypes.FORGET_MEMORY -> "Forget \"${intent.title}\" for good?"
        else -> "Go ahead?"
    }

    private inline fun <reified T : Enum<T>> enumOr(value: String?, fallback: T): T =
        value?.let { v -> enumValues<T>().firstOrNull { it.name.equals(v, ignoreCase = true) } } ?: fallback
}
