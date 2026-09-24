package com.arin.pocketcompanion.common

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
object Format {

    /** 418000 paise -> "₹4,180" (Indian digit grouping). */
    fun rupees(paise: Long): String {
        val rupees = paise / 100
        val rest = paise % 100
        val digits = rupees.toString()
        val grouped = if (digits.length <= 3) digits else {
            val head = digits.dropLast(3)
            head.reversed().chunked(2).joinToString(",").reversed() + "," + digits.takeLast(3)
        }
        return "₹" + grouped + if (rest != 0L) "." + rest.toString().padStart(2, '0') else ""
    }

    fun time(millis: Long, zone: TimeZone): String {
        val t = Instant.fromEpochMilliseconds(millis).toLocalDateTime(zone)
        return "${t.hour.toString().padStart(2, '0')}:${t.minute.toString().padStart(2, '0')}"
    }

    /** "20:00", "Tomorrow 10:00" or "Fri 10:00" style labels for the timeline. */
    fun whenLabel(millis: Long, nowMillis: Long, zone: TimeZone): String {
        val target = Instant.fromEpochMilliseconds(millis).toLocalDateTime(zone).date
        val today = Instant.fromEpochMilliseconds(nowMillis).toLocalDateTime(zone).date
        val days = target.toEpochDays().toLong() - today.toEpochDays().toLong()
        val prefix = when {
            days == 0L -> ""
            days == 1L -> "Tomorrow "
            days in 2..6 -> target.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() } + " "
            else -> dayLabel(millis, zone) + " "
        }
        return prefix + time(millis, zone)
    }

    fun isoToLabel(iso: String, nowMillis: Long, zone: TimeZone): String =
        runCatching { whenLabel(Instant.parse(iso).toEpochMilliseconds(), nowMillis, zone) }.getOrDefault(iso)

    /** "14 Oct" */
    fun dayLabel(millis: Long, zone: TimeZone): String =
        dateLabel(Instant.fromEpochMilliseconds(millis).toLocalDateTime(zone).date)

    fun dateLabel(date: LocalDate): String =
        "${date.dayOfMonth} ${date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }}"

    /** "Today", "Tomorrow", "Yesterday", "In 20 days" or "3 days ago", by calendar day. */
    fun relativeDay(millis: Long, nowMillis: Long, zone: TimeZone): String {
        val target = Instant.fromEpochMilliseconds(millis).toLocalDateTime(zone).date
        val today = Instant.fromEpochMilliseconds(nowMillis).toLocalDateTime(zone).date
        return when (val days = target.toEpochDays().toLong() - today.toEpochDays().toLong()) {
            0L -> "Today"
            1L -> "Tomorrow"
            -1L -> "Yesterday"
            in 2..Long.MAX_VALUE -> "In $days days"
            else -> "${-days} days ago"
        }
    }

    /** 5_400_000 -> "1h 30m"; under an hour -> "40 min"; never "0 min". */
    fun duration(millis: Long): String {
        val minutes = (millis / 60_000).coerceAtLeast(1)
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h == 0L -> "$m min"
            m == 0L -> "${h}h"
            else -> "${h}h ${m}m"
        }
    }

    /** "Thursday · 24 Sep" */
    fun headerDate(millis: Long, zone: TimeZone): String {
        val d = Instant.fromEpochMilliseconds(millis).toLocalDateTime(zone).date
        return d.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() } + " · " + dateLabel(d)
    }

    fun greeting(millis: Long, zone: TimeZone): String =
        when (Instant.fromEpochMilliseconds(millis).toLocalDateTime(zone).hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Up late"
        }
}
