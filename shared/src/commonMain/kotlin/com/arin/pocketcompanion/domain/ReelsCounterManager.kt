package com.arin.pocketcompanion.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MilestoneNudge(
    val count: Int,
    val title: String,
    val message: String,
    val primaryButton: String = "Back to Focus",
    val secondaryButton: String = "5 More Minutes",
    val tertiaryButton: String = "Keep Scrolling"
)

object ReelsCounterManager {
    private val _todayReelsCount = MutableStateFlow(0)
    val todayReelsCount: StateFlow<Int> = _todayReelsCount.asStateFlow()

    private val _activeMilestone = MutableStateFlow<MilestoneNudge?>(null)
    val activeMilestone: StateFlow<MilestoneNudge?> = _activeMilestone.asStateFlow()

    private val _isReelsAppForeground = MutableStateFlow(false)
    val isReelsAppForeground: StateFlow<Boolean> = _isReelsAppForeground.asStateFlow()

    private val _mutedUntilTimestamp = MutableStateFlow(0L)
    val mutedUntilTimestamp: StateFlow<Long> = _mutedUntilTimestamp.asStateFlow()

    private var lastNudgeMilestone = 0

    fun setReelsAppForeground(active: Boolean) {
        _isReelsAppForeground.value = active
    }

    fun muteForHours(hours: Int, nowMillis: Long) {
        _mutedUntilTimestamp.value = nowMillis + (hours * 3600 * 1000L)
        _activeMilestone.value = null
    }

    fun isMuted(nowMillis: Long): Boolean {
        return nowMillis < _mutedUntilTimestamp.value
    }

    fun incrementScroll() {
        _todayReelsCount.update { current ->
            val next = current + 1
            checkMilestone(next)
            next
        }
    }

    private fun checkMilestone(count: Int) {
        if (count > 0 && count % 100 == 0 && count != lastNudgeMilestone) {
            lastNudgeMilestone = count
            val nudge = when {
                count == 100 -> MilestoneNudge(
                    count = 100,
                    title = "100 Reels Milestone! 💯",
                    message = "You've watched 100 short videos! Ready to take a quick breather?"
                )
                count == 200 -> MilestoneNudge(
                    count = 200,
                    title = "200 Reels Milestone! 🚀",
                    message = "200 reels! Your thumb must be getting a workout 😅 Want to stretch?"
                )
                else -> MilestoneNudge(
                    count = count,
                    title = "$count Reels Milestone! 🏆",
                    message = "Master scroller! Time to take a break before your brain turns to jelly 🧠✨"
                )
            }
            _activeMilestone.value = nudge
        }
    }

    fun dismissMilestone() {
        _activeMilestone.value = null
    }

    fun resetCount() {
        _todayReelsCount.value = 0
        lastNudgeMilestone = 0
        _activeMilestone.value = null
    }
}
