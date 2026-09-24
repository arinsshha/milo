package com.arin.pocketcompanion.ui.memory

import com.arin.pocketcompanion.data.local.MemoryEntity
import com.arin.pocketcompanion.domain.MemoryKind
import com.arin.pocketcompanion.domain.Source
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MemoryListTest {
    private val zone = TimeZone.of("Asia/Kolkata")
    private fun day(d: Int, m: Int = 9) = LocalDate(2026, m, d).atStartOfDayIn(zone).toEpochMilliseconds()
    private val now = day(24) + 14 * 3_600_000L // 24 Sep, 14:00 IST

    private fun memory(
        id: String,
        kind: MemoryKind = MemoryKind.FACT,
        title: String = id,
        detail: String? = null,
        date: Long? = null,
        expiresAt: Long? = null,
        createdAt: Long = now,
    ) = MemoryEntity(id, kind, title, detail, date, expiresAt, Source.USER_SAID, createdAt)

    @Test fun groupsIntoSectionsInOrder() {
        val sections = MemoryList.build(
            listOf(
                memory("wifi", title = "Hostel wifi password", createdAt = now - 1000),
                memory("exam", MemoryKind.DATE, "DBMS exam", date = day(14, 10)),
                memory("bday", MemoryKind.DATE, "Riya's birthday", date = day(30)),
                memory("parking", MemoryKind.TEMPORARY, "Parked on level 2", expiresAt = now + 90 * 60_000),
                memory("tea", MemoryKind.PREFERENCE, "Likes tea, not coffee", createdAt = now),
            ),
            query = "", filter = MemoryFilter.All, now = now, zone = zone,
        )
        assertEquals(listOf("Coming up", "Forgetting soon", "Everything else"), sections.map { it.title })
        assertEquals(listOf("bday", "exam"), sections[0].rows.map { it.id }) // soonest first
        assertEquals(listOf("tea", "wifi"), sections[2].rows.map { it.id })  // newest first
    }

    @Test fun metaLabels() {
        val rows = MemoryList.build(
            listOf(
                memory("exam", MemoryKind.DATE, date = day(14, 10)),
                memory("today", MemoryKind.DATE, date = day(24)),
                memory("parking", MemoryKind.TEMPORARY, expiresAt = now + 90 * 60_000),
                memory("tea", MemoryKind.PREFERENCE, createdAt = day(20)),
            ),
            "", MemoryFilter.All, now, zone,
        ).flatMap { it.rows }.associate { it.id to it.meta }
        assertEquals("14 Oct · In 20 days", rows["exam"])
        assertEquals("24 Sep · Today", rows["today"])
        assertEquals("Forgets itself in 1h 30m", rows["parking"])
        assertEquals("Preference · saved 20 Sep", rows["tea"])
    }

    @Test fun pastDatesFallIntoEverythingElse() {
        val sections = MemoryList.build(
            listOf(memory("old", MemoryKind.DATE, date = day(20))), "", MemoryFilter.All, now, zone,
        )
        assertEquals("Everything else", sections.single().title)
        assertEquals("20 Sep · 4 days ago", sections.single().rows.single().meta)
    }

    @Test fun hidesExpired() {
        val sections = MemoryList.build(
            listOf(memory("gone", MemoryKind.TEMPORARY, expiresAt = now - 1)), "", MemoryFilter.All, now, zone,
        )
        assertTrue(sections.isEmpty())
    }

    @Test fun searchMatchesTitleOrDetailIgnoringCase() {
        val all = listOf(
            memory("a", title = "Hostel wifi password", detail = "hunter2"),
            memory("b", title = "Locker code", detail = "Near the WIFI router"),
            memory("c", title = "Bus number"),
        )
        val ids = MemoryList.build(all, "  wifi ", MemoryFilter.All, now, zone).flatMap { it.rows }.map { it.id }
        assertEquals(setOf("a", "b"), ids.toSet())
    }

    @Test fun filterByKind() {
        val all = listOf(
            memory("fact"),
            memory("date", MemoryKind.DATE, date = day(1, 10)),
            memory("temp", MemoryKind.TEMPORARY, expiresAt = now + 60_000),
        )
        val ids = MemoryList.build(all, "", MemoryFilter.Dates, now, zone).flatMap { it.rows }.map { it.id }
        assertEquals(listOf("date"), ids)
    }
}
