package com.arin.pocketcompanion.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arin.pocketcompanion.data.local.FocusDao
import com.arin.pocketcompanion.data.local.FocusSessionEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock

data class FocusUiState(
    val isActive: Boolean = false,
    val isBreak: Boolean = false,
    val remainingMillis: Long = 0L,
    val totalDurationMillis: Long = 0L
) {
    val progress: Float
        get() = if (totalDurationMillis > 0) (remainingMillis.toFloat() / totalDurationMillis).coerceIn(0f, 1f) else 0f
}

class FocusViewModel(private val focusDao: FocusDao) : ViewModel() {

    // A ticker flow that emits the current time every second
    private val clock = flow {
        while (true) {
            emit(Clock.System.now().toEpochMilliseconds())
            delay(1000)
        }
    }

    val state: StateFlow<FocusUiState> = combine(
        focusDao.observeActiveSession(Clock.System.now().toEpochMilliseconds()),
        clock
    ) { session, now ->
        if (session == null || session.endTime <= now) {
            FocusUiState()
        } else {
            val total = session.endTime - session.startTime
            val remaining = session.endTime - now
            FocusUiState(
                isActive = true,
                isBreak = session.isBreak,
                remainingMillis = remaining.coerceAtLeast(0L),
                totalDurationMillis = total
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FocusUiState())

    fun startFocusSession(minutes: Int) {
        val now = Clock.System.now().toEpochMilliseconds()
        val end = now + (minutes * 60 * 1000L)
        val id = "focus_$now"
        
        viewModelScope.launch {
            focusDao.clearAll() // Keep it simple, only one active session ever
            focusDao.upsert(FocusSessionEntity(id, now, end, isBreak = false))
        }
    }

    fun startBreak(minutes: Int) {
        val now = Clock.System.now().toEpochMilliseconds()
        val end = now + (minutes * 60 * 1000L)
        val id = "break_$now"

        viewModelScope.launch {
            focusDao.clearAll()
            focusDao.upsert(FocusSessionEntity(id, now, end, isBreak = true))
        }
    }

    fun endSession() {
        viewModelScope.launch {
            focusDao.clearAll()
        }
    }
}
