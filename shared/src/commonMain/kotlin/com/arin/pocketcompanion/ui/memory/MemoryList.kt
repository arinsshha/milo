package com.arin.pocketcompanion.ui.memory

import com.arin.pocketcompanion.common.Format
import com.arin.pocketcompanion.data.local.MemoryEntity
import com.arin.pocketcompanion.domain.MemoryKind
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

enum class MemoryFilter(val label: String, val kind: MemoryKind?) {
    All("All", null),
    Dates("Dates", MemoryKind.DATE),
    Facts("Facts", MemoryKind.FACT),
    Preferences("Preferences", MemoryKind.PREFERENCE),
    Temporary("Temporary", MemoryKind.TEMPORARY),
    Financial("Financial", MemoryKind.FINANCIAL),
    Routines("Routines", MemoryKind.ROUTINE)
}

data class MemoryRow(val id: String, val title: String, val detail: String?, val meta: String)
data class MemorySection(val title: String, val rows: List<MemoryRow>)

/** Turns raw memories into what the Memory screen shows. Pure, so it's tested without Room or Compose. */
@OptIn(ExperimentalTime::class)
object MemoryList {

    fun build(
        memories: List<MemoryEntity>,
        query: String,
        filter: MemoryFilter,
        now: Long,
        zone: TimeZone,
    ): List<MemorySection> {
        val q = query.trim()
        val visible = memories
            .filter { it.expiresAt == null || it.expiresAt > now }
            .filter { filter.kind == null || it.kind == filter.kind }
            .filter { q.isEmpty() || it.title.contains(q, ignoreCase = true) || it.detail?.contains(q, ignoreCase = true) == true }

        val startOfToday = Instant.fromEpochMilliseconds(now).toLocalDateTime(zone).date.atStartOfDayIn(zone).toEpochMilliseconds()
        val (upcoming, rest) = visible.partition { it.date != null && it.date >= startOfToday && it.expiresAt == null }
        val (temporary, other) = rest.partition { it.expiresAt != null }

        return listOf(
            MemorySection("Coming up", upcoming.sortedBy { it.date }.map { row(it, now, zone) }),
            MemorySection("Forgetting soon", temporary.sortedBy { it.expiresAt }.map { row(it, now, zone) }),
            MemorySection("Everything else", other.sortedByDescending { it.createdAt }.map { row(it, now, zone) }),
        ).filter { it.rows.isNotEmpty() }
    }

    private fun row(m: MemoryEntity, now: Long, zone: TimeZone): MemoryRow {
        val meta = when {
            m.expiresAt != null -> "Forgets itself in " + Format.duration(m.expiresAt - now)
            m.date != null -> Format.dayLabel(m.date, zone) + " · " + Format.relativeDay(m.date, now, zone)
            else -> kindLabel(m.kind) + " · saved " + Format.dayLabel(m.createdAt, zone)
        }
        return MemoryRow(m.id, m.title, m.detail?.takeIf { it.isNotBlank() }, meta)
    }

    private fun kindLabel(kind: MemoryKind): String = when (kind) {
        MemoryKind.DATE -> "Date"
        MemoryKind.FACT -> "Fact"
        MemoryKind.PREFERENCE -> "Preference"
        MemoryKind.TEMPORARY -> "Temporary"
        MemoryKind.FINANCIAL -> "Financial"
        MemoryKind.ROUTINE -> "Routine"
    }
}
