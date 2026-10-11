package com.arin.pocketcompanion.android.reminders

import android.app.KeyguardManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.arin.pocketcompanion.data.local.ReminderDao
import com.arin.pocketcompanion.data.local.ReminderEntity
import com.arin.pocketcompanion.domain.ReminderScheduler
import com.arin.pocketcompanion.domain.ReminderStatus
import com.arin.pocketcompanion.ui.reminders.AlarmScreen
import com.arin.pocketcompanion.ui.reminders.ReminderCompletedScreen
import com.arin.pocketcompanion.ui.theme.CompanionTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Dedicated, lock-screen-capable host for Milo's alarm UI. */
class AlarmActivity : ComponentActivity(), KoinComponent {
    private val reminderDao: ReminderDao by inject()
    private val scheduler: ReminderScheduler by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val id = intent.getStringExtra(ReminderReceiver.EXTRA_ID)
        if (id.isNullOrBlank()) {
            finish()
            return
        }

        lifecycleScope.launch {
            val reminder = withContext(Dispatchers.IO) { reminderDao.byId(id) }
            if (reminder == null || reminder.status != ReminderStatus.ACTIVE) {
                finish()
                return@launch
            }
            showReminder(reminder)
        }
    }

    private fun showReminder(reminder: ReminderEntity) {
        setContent {
            CompanionTheme {
                AlarmScreen(
                    reminder = reminder,
                    onGotIt = { finish() },
                    onComplete = {
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                val now = System.currentTimeMillis()
                                reminderDao.upsert(
                                    reminder.copy(
                                        status = ReminderStatus.DONE,
                                        completedAt = now
                                    )
                                )
                                scheduler.cancel(reminder.id)
                            }
                            finish()
                        }
                    },
                    onSnooze = { minutes ->
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                val trigger = System.currentTimeMillis() + minutes * 60_000L
                                val updated = reminder.copy(triggerAt = trigger)
                                reminderDao.upsert(updated)
                                scheduler.schedule(updated.id, updated.title, trigger, updated.rrule)
                            }
                            finish()
                        }
                    }
                )
            }
        }
    }
}
