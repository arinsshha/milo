package com.arin.pocketcompanion.di

import com.arin.pocketcompanion.data.local.AppDatabase
import com.arin.pocketcompanion.data.remote.CompanionApi
import com.arin.pocketcompanion.data.repository.IntentExecutor
import com.arin.pocketcompanion.data.repository.SettingsRepository
import com.arin.pocketcompanion.domain.InterventionEngine
import com.arin.pocketcompanion.ui.chat.ChatViewModel
import com.arin.pocketcompanion.ui.focus.FocusViewModel
import com.arin.pocketcompanion.ui.memory.MemoryViewModel
import com.arin.pocketcompanion.ui.money.MoneyViewModel
import com.arin.pocketcompanion.ui.reminders.RemindersViewModel
import com.arin.pocketcompanion.ui.settings.SettingsViewModel
import com.arin.pocketcompanion.ui.today.TodayViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Everything platform-neutral. The platform module must provide
 * AppDatabase and ReminderScheduler.
 */
fun sharedModule(apiBaseUrl: String): Module = module {
    single { Json { ignoreUnknownKeys = true; explicitNulls = false } }
    single {
        HttpClient {
            install(ContentNegotiation) { json(get()) }
            install(HttpTimeout) {
                requestTimeoutMillis = 20_000
                connectTimeoutMillis = 10_000
            }
        }
    }
    single { CompanionApi(get(), apiBaseUrl) }

    single { get<AppDatabase>().memoryDao() }
    single { get<AppDatabase>().reminderDao() }
    single { get<AppDatabase>().expenseDao() }
    single { get<AppDatabase>().interventionLogDao() }
    single { get<AppDatabase>().focusDao() }

    single { IntentExecutor(get(), get(), get(), get()) }
    single { InterventionEngine() }
    single { SettingsRepository() }

    viewModel { TodayViewModel(get(), get(), get()) }
    single { ChatViewModel(get(), get(), get(), getOrNull(), getOrNull()) }
    viewModel { MemoryViewModel(get(), get(), get()) }
    viewModel { FocusViewModel(get()) }
    viewModel { SettingsViewModel(get(), getOrNull()) }
    viewModel { MoneyViewModel(get(), get(), get()) }
    viewModel { RemindersViewModel(get(), get()) }
}
