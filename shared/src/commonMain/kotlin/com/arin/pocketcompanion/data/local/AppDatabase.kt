package com.arin.pocketcompanion.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [
        MemoryEntity::class,
        ReminderEntity::class,
        ExpenseEntity::class,
        TrackedAppEntity::class,
        InterventionLogEntity::class,
        FocusSessionEntity::class,
    ],
    version = 3,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun reminderDao(): ReminderDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun interventionLogDao(): InterventionLogDao
    abstract fun focusDao(): FocusDao
}

// Room's compiler generates the actual implementation for each platform.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
