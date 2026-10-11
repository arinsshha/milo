package com.arin.pocketcompanion.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(memory: MemoryEntity)

    @Query("SELECT * FROM memories WHERE expiresAt IS NULL OR expiresAt > :now ORDER BY createdAt DESC")
    fun observeActive(now: Long): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE expiresAt IS NULL OR expiresAt > :now ORDER BY createdAt DESC LIMIT :limit")
    suspend fun recent(now: Long, limit: Int): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE title LIKE '%' || :title || '%' LIMIT 1")
    suspend fun findByTitle(title: String): MemoryEntity?

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM memories WHERE expiresAt IS NOT NULL AND expiresAt <= :now")
    suspend fun deleteExpired(now: Long)
}

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders WHERE status = 'ACTIVE' AND triggerAt >= :from ORDER BY triggerAt LIMIT :limit")
    fun observeUpcoming(from: Long, limit: Int): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders ORDER BY triggerAt ASC")
    fun observeAll(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE status = 'DONE' ORDER BY completedAt DESC")
    fun observeCompleted(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE status = 'ACTIVE'")
    suspend fun allActive(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun byId(id: String): ReminderEntity?

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: ExpenseEntity)

    @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM expenses WHERE spentAt >= :from")
    fun observeTotalSince(from: Long): Flow<Long>

    @Query("SELECT * FROM expenses WHERE spentAt >= :from ORDER BY spentAt DESC")
    fun observeRecentSince(from: Long): Flow<List<ExpenseEntity>>

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface InterventionLogDao {
    @Insert
    suspend fun insert(entry: InterventionLogEntity)

    @Query("SELECT COUNT(*) FROM intervention_log WHERE shownAt >= :from AND level != 'SILENT'")
    suspend fun interruptionsSince(from: Long): Int

    @Query("SELECT * FROM intervention_log WHERE kind = :kind AND context = :context ORDER BY shownAt DESC LIMIT :limit")
    suspend fun recent(kind: String, context: String, limit: Int): List<InterventionLogEntity>
}

@Dao
interface FocusDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: FocusSessionEntity)

    @Query("SELECT * FROM focus_sessions WHERE endTime > :now ORDER BY endTime DESC LIMIT 1")
    fun observeActiveSession(now: Long): Flow<FocusSessionEntity?>

    @Query("SELECT * FROM focus_sessions WHERE endTime > :now ORDER BY endTime DESC LIMIT 1")
    suspend fun getActiveSession(now: Long): FocusSessionEntity?

    @Query("DELETE FROM focus_sessions WHERE id = :id")
    suspend fun delete(id: String)
    
    @Query("DELETE FROM focus_sessions")
    suspend fun clearAll()
}
