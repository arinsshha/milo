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

/** Fires when a reminder is due and presents the alarm-style full-screen notification. */
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
                val reminder = reminderDao.byId(id) ?: return@launch
                if (reminder.status != ReminderStatus.ACTIVE) return@launch

                val nowEpoch = System.currentTimeMillis()
                val recentLogs = interventionLogDao.recent("REMINDER", title, 3)
                val activeFocusSession = focusDao.getActiveSession(nowEpoch)
                val ctx = InterventionEngine.Context(
                    requested = NudgeLevel.GENTLE,
                    isQuietTime = activeFocusSession != null,
                    interruptionsToday = interventionLogDao.interruptionsSince(nowEpoch - 86400000),
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
                            response = NudgeResponse.IGNORED
                        )
                    )
                }

                // Schedule the next recurring occurrence, but do not mark a one-time reminder
                // completed just because it fired. Completion is the user's explicit action.
                val next = reminder.rrule?.let {
                    RRule.nextAfter(it, reminder.triggerAt, TimeZone.currentSystemDefault())
                }
                if (next != null) {
                    reminderDao.upsert(reminder.copy(triggerAt = next))
                    scheduler.schedule(reminder.id, reminder.title, next, reminder.rrule)
                }
            } finally {
                pending.finish()
            }
        }
    }

    private fun showNotification(context: Context, id: String, title: String, silent: Boolean = false) {
        val alarmIntent = Intent(context, AlarmActivity::class.java)
            .putExtra(EXTRA_ID, id)
            .putExtra(EXTRA_TITLE, title)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val alarmPending = PendingIntent.getActivity(
            context,
            id.hashCode(),
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Keep a normal notification as a fallback. Android may restrict full-screen
        // launches unless the user has allowed full-screen notifications for Milo.
        val openMain = PendingIntent.getActivity(
            context,
            (id + "_main").hashCode(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, AlarmReminderScheduler.CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }
        builder.setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText("Milo's reminding you 🙂")
            .setContentIntent(alarmPending)
            .setFullScreenIntent(alarmPending, !silent)
            .setCategory(Notification.CATEGORY_ALARM)
            .setPriority(if (silent) Notification.PRIORITY_DEFAULT else Notification.PRIORITY_MAX)
            .setAutoCancel(true)

        // The notification shade still offers a safe way to open the alarm screen.
        if (silent) builder.setContentIntent(openMain)
        context.getSystemService(NotificationManager::class.java)
            .notify(id.hashCode(), builder.build())
    }

    companion object {
        const val EXTRA_ID = "id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_RRULE = "rrule"
    }
}
