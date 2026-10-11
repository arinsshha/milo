package com.arin.pocketcompanion.android.reminders

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
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

import com.arin.pocketcompanion.domain.InterventionEngine
import com.arin.pocketcompanion.data.local.InterventionLogDao
import com.arin.pocketcompanion.data.local.InterventionLogEntity
import com.arin.pocketcompanion.domain.NudgeLevel
import com.arin.pocketcompanion.domain.NudgeResponse

import com.arin.pocketcompanion.data.local.FocusDao

/** Fires when a reminder is due: shows the notification, then books the next repeat if there is one. */
class ReminderReceiver : BroadcastReceiver(), KoinComponent {

    private val reminderDao: ReminderDao by inject()
    private val scheduler: ReminderScheduler by inject()
    private val interventionEngine: InterventionEngine by inject()
    private val interventionLogDao: InterventionLogDao by inject()
    private val focusDao: FocusDao by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(EXTRA_ID) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Check Intervention Engine
                val nowEpoch = System.currentTimeMillis()
                val todayStart = nowEpoch - 86400000 // roughly last 24h
                val recentLogs = interventionLogDao.recent("REMINDER", title, 3)
                val activeFocusSession = focusDao.getActiveSession(nowEpoch)
                val ctx = InterventionEngine.Context(
                    requested = NudgeLevel.GENTLE,
                    isQuietTime = activeFocusSession != null,
                    interruptionsToday = interventionLogDao.interruptionsSince(todayStart),
                    dailyBudget = 5,
                    userSaidNotToday = false,
                    recentResponses = recentLogs.map { it.response }
                )
                
                val decision = interventionEngine.decide(ctx)
                
                if (decision.shouldShow) {
                    showNotification(context, id, title, decision.level == NudgeLevel.SILENT)
                    interventionLogDao.insert(
                        InterventionLogEntity(
                            id = id + nowEpoch,
                            kind = "REMINDER",
                            level = decision.level ?: NudgeLevel.GENTLE,
                            context = title,
                            shownAt = nowEpoch,
                            response = NudgeResponse.IGNORED // default until user interacts
                        )
                    )
                }

                // 2. Handle Repeating Reminders
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

    private fun showNotification(context: Context, id: String, title: String, silent: Boolean = false) {
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        
        val builder = Notification.Builder(context, AlarmReminderScheduler.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder) // TODO: Milo notification icon
            .setContentTitle(title)
            .setContentText("Milo's reminding you 🙂")
            .setContentIntent(open)
            .setAutoCancel(true)

        // Silent visual cue
        if (silent && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setGroupAlertBehavior(Notification.GROUP_ALERT_SUMMARY) // Minimal interruption
        }

        context.getSystemService(NotificationManager::class.java).notify(id.hashCode(), builder.build())
    }

    companion object {
        const val EXTRA_ID = "id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_RRULE = "rrule"
    }
}
