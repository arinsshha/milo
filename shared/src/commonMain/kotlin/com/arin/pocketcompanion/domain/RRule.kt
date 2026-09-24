package com.arin.pocketcompanion.domain

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Tiny subset of RFC 5545 recurrence rules, enough for the MVP:
 * FREQ=DAILY, FREQ=WEEKLY, FREQ=MONTHLY;BYMONTHDAY=n.
 */
@OptIn(ExperimentalTime::class)
object RRule {

    fun isSupported(rrule: String): Boolean = parse(rrule)["FREQ"] in setOf("DAILY", "WEEKLY", "MONTHLY")

    /** Next occurrence strictly after [firedAtMillis], or null if the rule is unsupported. */
    fun nextAfter(rrule: String, firedAtMillis: Long, zone: TimeZone): Long? {
        val parts = parse(rrule)
        val fired = Instant.fromEpochMilliseconds(firedAtMillis).toLocalDateTime(zone)
        val nextDate: LocalDate = when (parts["FREQ"]) {
            "DAILY" -> fired.date.plus(1, DateTimeUnit.DAY)
            "WEEKLY" -> fired.date.plus(7, DateTimeUnit.DAY)
            "MONTHLY" -> {
                val firstOfNext = LocalDate(fired.year, fired.month, 1).plus(1, DateTimeUnit.MONTH)
                val firstOfAfter = firstOfNext.plus(1, DateTimeUnit.MONTH)
                val monthLength = (firstOfAfter.toEpochDays().toLong() - firstOfNext.toEpochDays().toLong()).toInt()
                val wanted = parts["BYMONTHDAY"]?.toIntOrNull() ?: 1
                firstOfNext.plus(wanted.coerceIn(1, monthLength) - 1, DateTimeUnit.DAY)
            }
            else -> return null
        }
        return LocalDateTime(nextDate, fired.time).toInstant(zone).toEpochMilliseconds()
    }

    private fun parse(rrule: String): Map<String, String> =
        rrule.removePrefix("RRULE:").split(';').mapNotNull {
            val p = it.split('=', limit = 2)
            if (p.size == 2) p[0].trim().uppercase() to p[1].trim().uppercase() else null
        }.toMap()
}
