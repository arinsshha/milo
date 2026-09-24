package com.arin.pocketcompanion.domain

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class RRuleTest {
    private val ist = TimeZone.of("Asia/Kolkata")
    private fun at(y: Int, m: Int, d: Int, h: Int = 9) = LocalDateTime(y, m, d, h, 0).toInstant(ist).toEpochMilliseconds()
    private fun local(ms: Long) = Instant.fromEpochMilliseconds(ms).toLocalDateTime(ist)

    @Test fun monthlyEmiMovesToTheFifthOfNextMonth() {
        val next = RRule.nextAfter("FREQ=MONTHLY;BYMONTHDAY=5", at(2026, 10, 5), ist)!!
        assertEquals(LocalDateTime(2026, 11, 5, 9, 0), local(next))
    }

    @Test fun day31ClampsInShortMonths() {
        val next = RRule.nextAfter("FREQ=MONTHLY;BYMONTHDAY=31", at(2027, 1, 31), ist)!!
        assertEquals(LocalDateTime(2027, 2, 28, 9, 0), local(next))
    }

    @Test fun weeklyAddsSevenDays() {
        val next = RRule.nextAfter("FREQ=WEEKLY", at(2026, 12, 28), ist)!!
        assertEquals(LocalDateTime(2027, 1, 4, 9, 0), local(next))
    }

    @Test fun unsupportedRuleReturnsNull() =
        assertNull(RRule.nextAfter("FREQ=YEARLY", at(2026, 1, 1), ist))
}
