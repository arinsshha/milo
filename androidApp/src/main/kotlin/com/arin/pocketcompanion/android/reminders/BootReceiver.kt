package com.arin.pocketcompanion.android.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.arin.pocketcompanion.data.local.ReminderDao
import com.arin.pocketcompanion.domain.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Android forgets every alarm on reboot. This puts them back. */
class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val reminderDao: ReminderDao by inject()
    private val scheduler: ReminderScheduler by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val now = System.currentTimeMillis()
                reminderDao.allActive().forEach {
                    // Missed while the phone was off? Fire it shortly after boot instead of never.
                    scheduler.schedule(it.id, it.title, maxOf(it.triggerAt, now + 5_000), it.rrule)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
