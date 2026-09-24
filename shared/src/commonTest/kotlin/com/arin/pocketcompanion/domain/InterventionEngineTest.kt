package com.arin.pocketcompanion.domain

import com.arin.pocketcompanion.domain.NudgeLevel.GENTLE
import com.arin.pocketcompanion.domain.NudgeLevel.SILENT
import com.arin.pocketcompanion.domain.NudgeLevel.URGENT
import com.arin.pocketcompanion.domain.NudgeResponse.ACCEPTED
import com.arin.pocketcompanion.domain.NudgeResponse.DISMISSED
import com.arin.pocketcompanion.domain.NudgeResponse.IGNORED
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class InterventionEngineTest {
    private val engine = InterventionEngine()
    private fun ctx(
        requested: NudgeLevel = GENTLE,
        quiet: Boolean = false,
        used: Int = 0,
        budget: Int = 6,
        notToday: Boolean = false,
        recent: List<NudgeResponse> = emptyList(),
    ) = InterventionEngine.Context(requested, quiet, used, budget, notToday, recent)

    @Test fun gentleNudgeGoesThroughWhenNothingBlocksIt() =
        assertEquals(GENTLE, engine.decide(ctx()).level)

    @Test fun quietTimeBlocksNonUrgent() =
        assertFalse(engine.decide(ctx(quiet = true)).shouldShow)

    @Test fun urgentBeatsQuietTime() =
        assertEquals(URGENT, engine.decide(ctx(requested = URGENT, quiet = true)).level)

    @Test fun notTodayMeansNotToday() =
        assertNull(engine.decide(ctx(notToday = true)).level)

    @Test fun threeDismissalsInARowGoSilent() =
        assertEquals(SILENT, engine.decide(ctx(recent = listOf(DISMISSED, IGNORED, DISMISSED))).level)

    @Test fun oneAcceptanceKeepsItGentle() =
        assertEquals(GENTLE, engine.decide(ctx(recent = listOf(DISMISSED, ACCEPTED, DISMISSED))).level)

    @Test fun spentBudgetDowngradesToSilent() =
        assertEquals(SILENT, engine.decide(ctx(used = 6, budget = 6)).level)
}
