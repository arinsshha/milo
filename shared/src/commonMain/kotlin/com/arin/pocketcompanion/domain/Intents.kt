package com.arin.pocketcompanion.domain

import kotlinx.serialization.Serializable

/**
 * The contract with the backend. The AI only *describes* what to do;
 * [com.arin.pocketcompanion.data.repository.IntentExecutor] decides whether and how to do it.
 */
@Serializable
data class InterpretRequest(
    val message: String,
    val now: String,          // ISO instant, UTC
    val timezone: String,     // e.g. "Asia/Kolkata"
    val memories: List<MemoryContext> = emptyList(),
)

@Serializable
data class MemoryContext(val title: String, val date: String? = null)

@Serializable
data class InterpretResponse(
    val reply: String,
    val intents: List<IntentDto> = emptyList(),
)

@Serializable
data class IntentDto(
    val type: String,
    val title: String? = null,
    val detail: String? = null,
    val triggerAt: String? = null,
    val rrule: String? = null,
    val priority: String? = null,
    val memoryKind: String? = null,
    val date: String? = null,
    val expiresInHours: Int? = null,
    val amountPaise: Long? = null,
    val category: String? = null,
    val note: String? = null,
    val confidence: Double = 0.0,
    val needsConfirmation: Boolean = false,
)

object IntentTypes {
    const val CREATE_REMINDER = "create_reminder"
    const val SAVE_MEMORY = "save_memory"
    const val LOG_EXPENSE = "log_expense"
    const val FORGET_MEMORY = "forget_memory"
    const val QUERY = "query"
    const val CHAT = "chat"
}
