package com.arin.pocketcompanion.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arin.pocketcompanion.data.local.ExpenseDao
import com.arin.pocketcompanion.data.local.MemoryDao
import com.arin.pocketcompanion.data.local.ReminderDao
import com.arin.pocketcompanion.domain.Priority
import com.arin.pocketcompanion.common.Format
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class TimelineItem(val time: String, val title: String, val sub: String)
data class RightNow(val title: String, val detail: String)

data class TodayUiState(
    val greeting: String = "",
    val dateLabel: String = "",
    val rightNow: RightNow? = null,
    val timeline: List<TimelineItem> = emptyList(),
    val monthSpent: String = "₹0",
    val memoriesCount: Int = 0,
)

@OptIn(ExperimentalTime::class)
class TodayViewModel(
    reminderDao: ReminderDao,
    expenseDao: ExpenseDao,
    memoryDao: MemoryDao,
) : ViewModel() {

    private val zone = TimeZone.currentSystemDefault()
    private val now = Clock.System.now().toEpochMilliseconds()
    private val startOfMonth: Long = run {
        val today = Clock.System.now().toLocalDateTime(zone).date
        LocalDate(today.year, today.month, 1).atStartOfDayIn(zone).toEpochMilliseconds()
    }

    val state: StateFlow<TodayUiState> = combine(
        reminderDao.observeUpcoming(from = now, limit = 5),
        expenseDao.observeTotalSince(startOfMonth),
        memoryDao.observeActive(now),
    ) { reminders, spent, memories ->
        val focus = reminders.firstOrNull { it.priority == Priority.IMPORTANT } ?: reminders.firstOrNull()
        TodayUiState(
            greeting = Format.greeting(now, zone),
            dateLabel = Format.headerDate(now, zone),
            rightNow = focus?.let { RightNow(it.title, "Coming up at " + Format.whenLabel(it.triggerAt, now, zone)) },
            timeline = reminders.map {
                TimelineItem(
                    time = Format.whenLabel(it.triggerAt, now, zone),
                    title = it.title,
                    sub = if (it.priority == Priority.IMPORTANT) "Important" else "Reminder",
                )
            },
            monthSpent = Format.rupees(spent),
            memoriesCount = memories.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState())
}
