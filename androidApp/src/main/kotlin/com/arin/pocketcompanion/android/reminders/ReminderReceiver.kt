package com.arin.pocketcompanion.android.reminders

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.arin.pocketcompanion.android.MainActivity
import com.arin.pocketcompanion.data.local.ReminderDao
import com.arin.pocketcompanion.domain.RRule
import com.arin.pocketcompanion.domain.ReminderScheduler
import com.arin.pocketcompanion.domain.ReminderStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Fires when a reminder is due: shows the notification, then books the next repeat if there is one. */
class ReminderReceiver : BroadcastReceiver(), KoinComponent {

    private val reminderDao: ReminderDao by inject()
    private val scheduler: ReminderScheduler by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(EXTRA_ID) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        showNotification(context, id, title)

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminder = reminderDao.byId(id) ?: return@launch
                val next = reminder.rrule?.let { RRule.nextAfter(it, reminder.triggerAt, TimeZone.currentSystemDefault()) }
                if (next != null) {
                    reminderDao.upsert(reminder.copy(triggerAt = next))
                    scheduler.schedule(reminder.id, reminder.title, next, reminder.rrule)
                } else {
                    reminderDao.upsert(reminder.copy(status = ReminderStatus.DONE))
                }
            } finally {
                pending.finish()
            }
        }
    }

    private fun showNotification(context: Context, id: String, title: String) {
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = Notification.Builder(context, AlarmReminderScheduler.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder) // TODO: Milo notification icon
            .setContentTitle(title)
            .setContentText("Milo's reminding you 🙂")
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(id.hashCode(), notification)
    }

    companion object {
        const val EXTRA_ID = "id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_RRULE = "rrule"
    }
}
