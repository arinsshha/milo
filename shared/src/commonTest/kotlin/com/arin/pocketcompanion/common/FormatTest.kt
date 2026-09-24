package com.arin.pocketcompanion.common

import kotlin.test.Test
import kotlin.test.assertEquals

class FormatTest {
    @Test fun indianGrouping() {
        assertEquals("₹250", Format.rupees(25_000))
        assertEquals("₹4,180", Format.rupees(418_000))
        assertEquals("₹12,34,567", Format.rupees(123_456_700))
        assertEquals("₹99.50", Format.rupees(9_950))
    }

    @Test fun durations() {
        assertEquals("1 min", Format.duration(5_000))
        assertEquals("40 min", Format.duration(40 * 60_000L))
        assertEquals("2h", Format.duration(120 * 60_000L))
        assertEquals("1h 30m", Format.duration(90 * 60_000L))
    }
}
