package com.arin.pocketcompanion.android.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.arin.pocketcompanion.domain.ReminderScheduler

class AlarmReminderScheduler(private val context: Context) : ReminderScheduler {

    private val alarms = context.getSystemService(AlarmManager::class.java)

    override fun schedule(id: String, title: String, triggerAtMillis: Long, rrule: String?) {
        val pending = pendingIntent(id, title, rrule)
        val canExact = Build.VERSION.SDK_INT < 31 || alarms.canScheduleExactAlarms()
        if (canExact) {
            alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        } else {
            // Without exact-alarm permission Android may delay this by a few minutes. Fine for most reminders.
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        }
    }

    override fun cancel(id: String) {
        alarms.cancel(pendingIntent(id, "", null))
    }

    private fun pendingIntent(id: String, title: String, rrule: String?): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .setAction("com.arin.pocketcompanion.REMINDER.$id") // unique action = unique PendingIntent per reminder
            .putExtra(ReminderReceiver.EXTRA_ID, id)
            .putExtra(ReminderReceiver.EXTRA_TITLE, title)
            .putExtra(ReminderReceiver.EXTRA_RRULE, rrule)
        return PendingIntent.getBroadcast(
            context, id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val CHANNEL_ID = "reminders"

        fun createChannel(context: Context) {
            val channel = NotificationChannel(CHANNEL_ID, "Reminders", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Reminders you asked Milo to set" }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
