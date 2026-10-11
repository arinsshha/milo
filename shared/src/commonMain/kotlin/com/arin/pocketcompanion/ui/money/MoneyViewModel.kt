package com.arin.pocketcompanion.ui.money

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arin.pocketcompanion.common.Format
import com.arin.pocketcompanion.data.local.ExpenseDao
import com.arin.pocketcompanion.data.remote.CompanionApi
import com.arin.pocketcompanion.data.repository.IntentExecutor
import com.arin.pocketcompanion.domain.InterpretRequest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class ExpenseRow(
    val id: String,
    val amountFormatted: String,
    val category: String,
    val note: String?,
    val dateFormatted: String
)

data class CategoryShare(
    val category: String,
    val shareFraction: Float,
    val percentageLabel: String,
    val amountFormatted: String
)

data class MoneyUiState(
    val totalFormatted: String = "₹0",
    val totalPaise: Long = 0L,
    val expenses: List<ExpenseRow> = emptyList(),
    val categoryShares: List<CategoryShare> = emptyList(),
    val goalPaise: Long = 2000000L, // ₹20,000 budget / goal
    val savedPaise: Long = 2000000L,
    val isSending: Boolean = false
) {
    val savingsProgress: Float
        get() = if (goalPaise > 0) (savedPaise.toFloat() / goalPaise).coerceIn(0f, 1f) else 0f

    val savedFormatted: String
        get() = Format.rupees(savedPaise)

    val goalFormatted: String
        get() = Format.rupees(goalPaise)
}

@OptIn(ExperimentalTime::class)
class MoneyViewModel(
    private val expenseDao: ExpenseDao,
    private val api: CompanionApi,
    private val executor: IntentExecutor
) : ViewModel() {

    private val zone = TimeZone.currentSystemDefault()

    private fun startOfMonthMillis(): Long {
        val nowInstant = Clock.System.now()
        val ldt = nowInstant.toLocalDateTime(zone)
        val monthStart = LocalDate(ldt.year, ldt.month, 1)
        return monthStart.atStartOfDayIn(zone).toEpochMilliseconds()
    }

    val state: StateFlow<MoneyUiState> = combine(
        expenseDao.observeTotalSince(startOfMonthMillis()),
        expenseDao.observeRecentSince(startOfMonthMillis())
    ) { totalPaise, rawExpenses ->
        val rows = rawExpenses.map { e ->
            ExpenseRow(
                id = e.id,
                amountFormatted = Format.rupees(e.amountPaise),
                category = e.category,
                note = e.note,
                dateFormatted = Format.dayLabel(e.spentAt, zone)
            )
        }

        // Calculate dynamic category percentages
        val categoryGrouped = rawExpenses.groupBy { it.category }
        val shares = categoryGrouped.map { (cat, list) ->
            val catTotal = list.sumOf { it.amountPaise }
            val fraction = if (totalPaise > 0) catTotal.toFloat() / totalPaise else 0f
            val pct = (fraction * 100).toInt()
            CategoryShare(
                category = cat,
                shareFraction = fraction,
                percentageLabel = "$pct%",
                amountFormatted = Format.rupees(catTotal)
            )
        }.sortedByDescending { it.shareFraction }

        // Budget remaining calculation (Budget = ₹20,000)
        val monthlyBudgetPaise = 2000000L
        val remainingBudgetPaise = (monthlyBudgetPaise - totalPaise).coerceAtLeast(0L)

        MoneyUiState(
            totalFormatted = Format.rupees(totalPaise),
            totalPaise = totalPaise,
            expenses = rows,
            categoryShares = shares,
            goalPaise = monthlyBudgetPaise,
            savedPaise = remainingBudgetPaise
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MoneyUiState())

    fun logExpense(text: String) {
        val message = text.trim()
        if (message.isEmpty()) return

        viewModelScope.launch {
            val now = Clock.System.now()
            api.interpret(InterpretRequest(message, now.toString(), zone.id, emptyList()))
                .onSuccess { response ->
                    response.intents.forEach { executor.execute(it) }
                }
        }
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch {
            expenseDao.delete(id)
        }
    }
}
