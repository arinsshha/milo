package com.arin.pocketcompanion.android

import android.app.Application
import com.arin.pocketcompanion.android.reminders.AlarmReminderScheduler
import com.arin.pocketcompanion.data.local.AppDatabase
import com.arin.pocketcompanion.data.local.buildDatabase
import com.arin.pocketcompanion.di.sharedModule
import com.arin.pocketcompanion.domain.ReminderScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class PocketCompanionApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AlarmReminderScheduler.createChannel(this)
        startKoin {
            androidContext(this@PocketCompanionApp)
            modules(
                sharedModule(apiBaseUrl = BuildConfig.API_BASE_URL),
                module {
                    single<AppDatabase> { buildDatabase(androidContext()) }
                    single<ReminderScheduler> { AlarmReminderScheduler(androidContext()) }
                },
            )
        }
    }
}
