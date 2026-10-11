package com.arin.pocketcompanion.ui.money

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arin.pocketcompanion.ui.components.Milo
import com.arin.pocketcompanion.ui.theme.DisplayFont
import com.arin.pocketcompanion.ui.theme.Palette
import com.arin.pocketcompanion.ui.today.card
import org.koin.compose.koinInject

@Composable
fun MoneyScreen(viewModel: MoneyViewModel = koinInject()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var expenseText by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Finances", fontFamily = DisplayFont, fontSize = 30.sp, fontWeight = FontWeight.SemiBold, color = Palette.Ink)
                    Text("Track your monthly expenses with Milo", color = Palette.Muted, fontSize = 14.sp)
                }
            }

            // Total spent card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Palette.Teal),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("SPENT THIS MONTH", color = Palette.TealOnDark, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.7.sp)
                        Text(state.totalFormatted, fontFamily = DisplayFont, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Palette.Surface)
                    }
                }
            }

            // Spending Breakdown Donut Chart
            item {
                SpendingBreakdownCard(shares = state.categoryShares)
            }

            // Savings Goal Progress
            item {
                SavingsGoalCard(
                    savedFormatted = state.savedFormatted,
                    goalFormatted = state.goalFormatted,
                    progress = state.savingsProgress
                )
            }

            // Recent Expenses Section
            item {
                Text("Recent Transactions", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Palette.Ink)
            }

            if (state.expenses.isEmpty()) {
                item {
                    Row(
                        modifier = Modifier.card().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Milo(size = 44.dp)
                        Text(
                            "No expenses logged this month yet. Tell Milo \"spent 250 on lunch\" to record one!",
                            color = Palette.Muted, fontSize = 14.sp, lineHeight = 20.sp,
                        )
                    }
                }
            } else {
                items(state.expenses, key = { it.id }) { row ->
                    Column(modifier = Modifier.card()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(row.note ?: row.category, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Palette.Ink)
                                Text("${row.category} · ${row.dateFormatted}", fontSize = 13.sp, color = Palette.Muted)
                            }
                            Text(row.amountFormatted, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Palette.Ink)
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = { viewModel.deleteExpense(row.id) },
                                modifier = Modifier.heightIn(min = 44.dp)
                            ) {
                                Text("Delete", color = Palette.Muted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Bottom manual expense logging bar
        Box(modifier = Modifier.fillMaxSize().imePadding(), contentAlignment = Alignment.BottomCenter) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Palette.Surface)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = expenseText,
                    onValueChange = { expenseText = it },
                    placeholder = { Text("Log expense e.g. \"spent 250 on lunch\"...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Palette.Paper,
                        unfocusedContainerColor = Palette.Paper,
                        focusedIndicatorColor = Palette.Paper,
                        unfocusedIndicatorColor = Palette.Paper,
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (expenseText.isNotBlank()) {
                            viewModel.logExpense(expenseText)
                            expenseText = ""
                        }
                    },
                    modifier = Modifier.background(Palette.Teal, CircleShape)
                ) {
                    Text("↑", color = Palette.Surface, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
private fun SpendingBreakdownCard(shares: List<CategoryShare>) {
    val categoryColors = listOf(
        Palette.Teal,
        Color(0xFF438A7A),
        Color(0xFFEE9A5E),
        Color(0xFFC8662F),
        Color(0xFF7E776E),
        Palette.Line
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Palette.Surface),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Spending Breakdown", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Palette.Ink)

            if (shares.isEmpty()) {
                Text(
                    text = "No spending breakdown yet. Log your first expense below to see category shares!",
                    fontSize = 13.sp,
                    color = Palette.Muted
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Donut Chart
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(110.dp)
                    ) {
                        Canvas(modifier = Modifier.size(100.dp)) {
                            val strokeWidth = 24.dp.toPx()
                            var startAngle = -90f

                            shares.forEachIndexed { i, share ->
                                val sweep = share.shareFraction * 360f
                                if (sweep > 0f) {
                                    val color = categoryColors[i % categoryColors.size]
                                    drawArc(
                                        color = color,
                                        startAngle = startAngle,
                                        sweepAngle = (sweep - 2f).coerceAtLeast(1f),
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth)
                                    )
                                    startAngle += sweep
                                }
                            }
                        }
                    }

                    // Legend
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        shares.forEachIndexed { i, share ->
                            val color = categoryColors[i % categoryColors.size]
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
                                Text(share.category, fontSize = 13.sp, color = Palette.Ink, modifier = Modifier.width(80.dp))
                                Text(share.percentageLabel, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Palette.Ink)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavingsGoalCard(
    savedFormatted: String,
    goalFormatted: String,
    progress: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Palette.Surface),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Monthly Budget Remaining", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Palette.Ink)
                Text("${(progress * 100).toInt()}% left", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Palette.Teal)
            }
            Text("$savedFormatted left of $goalFormatted budget", fontFamily = DisplayFont, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Palette.Ink)

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = Palette.Teal,
                trackColor = Palette.Line,
            )
        }
    }
}
