package com.arin.pocketcompanion.domain

/** Implemented per platform: AlarmManager on Android, UNUserNotificationCenter on iOS later. */
interface ReminderScheduler {
    fun schedule(id: String, title: String, triggerAtMillis: Long, rrule: String?)
    fun cancel(id: String)
}
