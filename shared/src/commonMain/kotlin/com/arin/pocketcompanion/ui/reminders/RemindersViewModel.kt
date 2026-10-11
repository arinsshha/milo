package com.arin.pocketcompanion.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arin.pocketcompanion.data.local.ReminderDao
import com.arin.pocketcompanion.data.local.ReminderEntity
import com.arin.pocketcompanion.domain.Priority
import com.arin.pocketcompanion.domain.RRule
import com.arin.pocketcompanion.domain.ReminderScheduler
import com.arin.pocketcompanion.domain.ReminderStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

enum class ReminderFilter(val label: String) {
    All("All"),
    Today("Today"),
    Upcoming("Upcoming"),
    Completed("Completed")
}

data class RemindersUiState(
    val filter: ReminderFilter = ReminderFilter.All,
    val reminders: List<ReminderEntity> = emptyList(),
    val activeAlarmReminder: ReminderEntity? = null,
    val isSnoozing: Boolean = false,
    val completedCelebration: ReminderEntity? = null
)

@OptIn(ExperimentalTime::class)
class RemindersViewModel(
    private val reminderDao: ReminderDao,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val zone = TimeZone.currentSystemDefault()
    private val filter = MutableStateFlow(ReminderFilter.All)
    private val activeAlarm = MutableStateFlow<ReminderEntity?>(null)
    private val celebration = MutableStateFlow<ReminderEntity?>(null)

    private fun startOfTodayMillis(): Long {
        val nowInstant = Clock.System.now()
        val ldt = nowInstant.toLocalDateTime(zone)
        val todayStart = LocalDate(ldt.year, ldt.month, ldt.dayOfMonth)
        return todayStart.atStartOfDayIn(zone).toEpochMilliseconds()
    }

    private fun endOfTodayMillis(): Long {
        return startOfTodayMillis() + (86400 * 1000L) - 1L
    }

    val state: StateFlow<RemindersUiState> = combine(
        reminderDao.observeAll(),
        reminderDao.observeCompleted(),
        filter,
        activeAlarm,
        celebration
    ) { all, completed, f, alarm, celeb ->
        val now = Clock.System.now().toEpochMilliseconds()
        val startToday = startOfTodayMillis()
        val endToday = endOfTodayMillis()

        val filteredList = when (f) {
            ReminderFilter.All -> all.filter { it.status == ReminderStatus.ACTIVE }
            ReminderFilter.Today -> all.filter { it.status == ReminderStatus.ACTIVE && it.triggerAt in startToday..endToday }
            ReminderFilter.Upcoming -> all.filter { it.status == ReminderStatus.ACTIVE && it.triggerAt > now }
            ReminderFilter.Completed -> completed
        }

        RemindersUiState(
            filter = f,
            reminders = filteredList,
            activeAlarmReminder = alarm,
            completedCelebration = celeb
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RemindersUiState())

    fun setFilter(f: ReminderFilter) {
        filter.value = f
    }

    fun saveReminder(
        id: String? = null,
        title: String,
        triggerAt: Long,
        rrule: String? = null,
        category: String = "Personal",
        note: String? = null,
        ringtoneId: String = "Milo's Chime",
        priority: Priority = Priority.NORMAL
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return

        val reminderId = id ?: "rem_${Clock.System.now().toEpochMilliseconds()}"
        val entity = ReminderEntity(
            id = reminderId,
            title = cleanTitle,
            triggerAt = triggerAt,
            rrule = rrule,
            priority = priority,
            status = ReminderStatus.ACTIVE,
            category = category,
            note = note?.takeIf { it.isNotBlank() },
            ringtoneId = ringtoneId,
            createdAt = Clock.System.now().toEpochMilliseconds()
        )

        viewModelScope.launch {
            reminderDao.upsert(entity)
            reminderScheduler.schedule(entity.id, entity.title, entity.triggerAt, entity.rrule)
        }
    }

    fun toggleReminder(reminder: ReminderEntity) {
        val newStatus = if (reminder.status == ReminderStatus.ACTIVE) ReminderStatus.CANCELLED else ReminderStatus.ACTIVE
        val updated = reminder.copy(status = newStatus)
        
        viewModelScope.launch {
            reminderDao.upsert(updated)
            if (newStatus == ReminderStatus.ACTIVE) {
                reminderScheduler.schedule(updated.id, updated.title, updated.triggerAt, updated.rrule)
            } else {
                reminderScheduler.cancel(updated.id)
            }
        }
    }

    fun completeReminder(reminder: ReminderEntity) {
        val now = Clock.System.now().toEpochMilliseconds()
        val nextOccurrence = reminder.rrule?.let { RRule.nextAfter(it, reminder.triggerAt, zone) }

        viewModelScope.launch {
            if (nextOccurrence != null) {
                // For recurring, update triggerAt to next occurrence
                val recurringUpdated = reminder.copy(triggerAt = nextOccurrence, completedAt = now)
                reminderDao.upsert(recurringUpdated)
                reminderScheduler.schedule(recurringUpdated.id, recurringUpdated.title, nextOccurrence, recurringUpdated.rrule)
            } else {
                // Mark DONE
                val doneUpdated = reminder.copy(status = ReminderStatus.DONE, completedAt = now)
                reminderDao.upsert(doneUpdated)
                reminderScheduler.cancel(reminder.id)
            }
            activeAlarm.value = null
            celebration.value = reminder
        }
    }

    fun snoozeReminder(reminder: ReminderEntity, minutes: Int) {
        val now = Clock.System.now().toEpochMilliseconds()
        val snoozedTime = now + (minutes * 60 * 1000L)
        val updated = reminder.copy(triggerAt = snoozedTime)

        viewModelScope.launch {
            reminderDao.upsert(updated)
            reminderScheduler.schedule(updated.id, updated.title, snoozedTime, updated.rrule)
            activeAlarm.value = null
        }
    }

    fun deleteReminder(id: String) {
        viewModelScope.launch {
            reminderDao.delete(id)
            reminderScheduler.cancel(id)
        }
    }

    fun triggerAlarmTest(reminder: ReminderEntity) {
        activeAlarm.value = reminder
    }

    fun dismissAlarm() {
        activeAlarm.value = null
    }

    fun dismissCelebration() {
        celebration.value = null
    }
}
