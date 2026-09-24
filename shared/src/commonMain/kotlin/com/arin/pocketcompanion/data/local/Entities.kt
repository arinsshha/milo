package com.arin.pocketcompanion.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arin.pocketcompanion.domain.AppCategory
import com.arin.pocketcompanion.domain.MemoryKind
import com.arin.pocketcompanion.domain.NudgeLevel
import com.arin.pocketcompanion.domain.NudgeResponse
import com.arin.pocketcompanion.domain.Priority
import com.arin.pocketcompanion.domain.ReminderStatus
import com.arin.pocketcompanion.domain.Source

// All timestamps are epoch millis (UTC). Money is always paise (Long), never Float.

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey val id: String,
    val kind: MemoryKind,
    val title: String,
    val detail: String?,
    val date: Long?,
    val expiresAt: Long?,
    val source: Source,
    val createdAt: Long,
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val title: String,
    val triggerAt: Long,
    val rrule: String?,
    val priority: Priority,
    val memoryId: String?,
    val status: ReminderStatus,
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val amountPaise: Long,
    val category: String,
    val note: String?,
    val spentAt: Long,
)

@Entity(tableName = "tracked_apps")
data class TrackedAppEntity(
    @PrimaryKey val packageName: String,
    val label: String,
    val category: AppCategory,
    val thresholdMinutes: Int,
)

@Entity(tableName = "intervention_log")
data class InterventionLogEntity(
    @PrimaryKey val id: String,
    val kind: String,
    val level: NudgeLevel,
    val context: String,
    val shownAt: Long,
    val response: NudgeResponse,
)
