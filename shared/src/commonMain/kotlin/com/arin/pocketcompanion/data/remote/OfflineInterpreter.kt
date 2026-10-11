package com.arin.pocketcompanion.data.remote

import com.arin.pocketcompanion.domain.IntentDto
import com.arin.pocketcompanion.domain.IntentTypes
import com.arin.pocketcompanion.domain.InterpretRequest
import com.arin.pocketcompanion.domain.InterpretResponse
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object OfflineInterpreter {

    @OptIn(ExperimentalTime::class)
    fun interpret(request: InterpretRequest): InterpretResponse {
        val msg = request.message.trim()
        val lower = msg.lowercase()

        // 1. Expense Pattern: "spent 250 on lunch", "paid 1200 for rent", "150 for coffee"
        val expenseRegex = Regex("""(?:spent|paid|cost)?\s*₹?\s*\$?(\d+(?:\.\d{1,2})?)\s*(?:on|for|at)?\s*(.+)?""", RegexOption.IGNORE_CASE)
        val matchExpense = expenseRegex.find(msg)
        if ((lower.contains("spent") || lower.contains("paid") || lower.contains("cost")) && matchExpense != null) {
            val amount = matchExpense.groupValues[1].toDoubleOrNull()
            if (amount != null) {
                val item = matchExpense.groupValues.getOrNull(2)?.trim()?.ifBlank { "General" } ?: "General"
                val paise = (amount * 100).toLong()
                val category = when {
                    item.contains("lunch", true) || item.contains("dinner", true) || item.contains("food", true) || item.contains("coffee", true) -> "Food"
                    item.contains("bus", true) || item.contains("uber", true) || item.contains("cab", true) || item.contains("fuel", true) -> "Transport"
                    item.contains("rent", true) || item.contains("bill", true) || item.contains("electricity", true) -> "Bills"
                    item.contains("shirt", true) || item.contains("clothes", true) || item.contains("shopping", true) -> "Shopping"
                    else -> "Other"
                }

                return InterpretResponse(
                    reply = "Got it! Logged $item for ₹${amount.toLong()}.",
                    intents = listOf(
                        IntentDto(
                            type = IntentTypes.LOG_EXPENSE,
                            amountPaise = paise,
                            category = category,
                            note = item,
                            confidence = 0.95
                        )
                    )
                )
            }
        }

        // 2. Memory Pattern: "remember my exam is on 14 Oct", "remember I like green tea"
        if (lower.startsWith("remember") || lower.contains("remember ")) {
            val content = msg.substringAfter("remember", "").substringAfter("my", "").trim().ifBlank { msg }
            val isDate = lower.contains("on ") || lower.contains("by ") || lower.contains("oct") || lower.contains("nov") || lower.contains("dec")

            return InterpretResponse(
                reply = "I've stored that in your memories!",
                intents = listOf(
                    IntentDto(
                        type = IntentTypes.SAVE_MEMORY,
                        title = content.replaceFirstChar { it.uppercase() },
                        memoryKind = if (isDate) "DATE" else "FACT",
                        confidence = 0.9
                    )
                )
            )
        }

        // 3. Reminder Pattern: "remind me to pay EMI on the 5th"
        if (lower.contains("remind me") || lower.startsWith("remind ")) {
            val content = msg.substringAfter("remind me to", "").substringAfter("remind me", "").substringAfter("remind", "").trim()
            val nowMillis = Clock.System.now().toEpochMilliseconds()
            val triggerAt = nowMillis + (3600 * 1000 * 2) // default +2 hours

            return InterpretResponse(
                reply = "Reminder set for $content!",
                intents = listOf(
                    IntentDto(
                        type = IntentTypes.CREATE_REMINDER,
                        title = content.replaceFirstChar { it.uppercase() },
                        triggerAt = Instant.fromEpochMilliseconds(triggerAt).toString(),
                        confidence = 0.9
                    )
                )
            )
        }

        // 4. Quick Prompts & Conversation
        val reply = when {
            lower.contains("anxious") || lower.contains("stress") ->
                "Take a deep breath with me. 🧘 You've handled so much already today. Try a quick 1-minute breathing exercise in the Focus tab whenever you're ready."

            lower.contains("motivation") || lower.contains("inspire") ->
                "Small steps lead to brighter days! What is one tiny 5-minute task we can finish together right now?"

            lower.contains("decision") ->
                "I'm here to help you weigh it out. Tell me the top two choices you're debating!"

            lower.contains("hi") || lower.contains("hello") || lower.contains("hey") ->
                "Hey there! I'm Milo, your pocket companion. I'm here for your thoughts, goals, and everyday moments."

            else ->
                "I hear you! I'm keeping track of everything you share with me."
        }

        return InterpretResponse(reply = reply)
    }
}
