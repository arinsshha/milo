package com.arin.pocketcompanion.ui.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arin.pocketcompanion.common.Format
import com.arin.pocketcompanion.data.local.ReminderEntity
import com.arin.pocketcompanion.domain.ReminderStatus
import com.arin.pocketcompanion.ui.components.Milo
import com.arin.pocketcompanion.ui.theme.DisplayFont
import com.arin.pocketcompanion.ui.theme.Palette
import com.arin.pocketcompanion.ui.today.card
import kotlinx.datetime.TimeZone
import org.koin.compose.koinInject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun RemindersListScreen(
    onNewReminder: () -> Unit,
    onEditReminder: (ReminderEntity) -> Unit,
    onTestAlarm: (ReminderEntity) -> Unit,
    viewModel: RemindersViewModel = koinInject()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val zone = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toEpochMilliseconds()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Reminders", fontFamily = DisplayFont, fontSize = 30.sp, fontWeight = FontWeight.SemiBold, color = Palette.Ink)
                Text("Small reminders. Big changes.", color = Palette.Muted, fontSize = 14.sp)
            }

            IconButton(
                onClick = onNewReminder,
                modifier = Modifier
                    .size(48.dp)
                    .background(Palette.Teal, CircleShape)
            ) {
                Text("+", color = Palette.Surface, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Filter Tabs
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReminderFilter.entries.forEach { f ->
                FilterChip(
                    selected = f == state.filter,
                    onClick = { viewModel.setFilter(f) },
                    label = { Text(f.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Palette.Surface,
                        labelColor = Palette.Muted,
                        selectedContainerColor = Palette.TealSoft,
                        selectedLabelColor = Palette.Teal,
                    )
                )
            }
        }

        // Reminder List
        if (state.reminders.isEmpty()) {
            Row(
                modifier = Modifier.card().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Milo(size = 44.dp)
                Text(
                    "No reminders here yet. Let Milo help you remember the little things.",
                    color = Palette.Muted, fontSize = 14.sp, lineHeight = 20.sp,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.reminders, key = { it.id }) { reminder ->
                    ReminderCardRow(
                        reminder = reminder,
                        now = now,
                        zone = zone,
                        onToggle = { viewModel.toggleReminder(reminder) },
                        onClick = { onEditReminder(reminder) },
                        onTestAlarm = { onTestAlarm(reminder) },
                        onDelete = { viewModel.deleteReminder(reminder.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderCardRow(
    reminder: ReminderEntity,
    now: Long,
    zone: TimeZone,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onTestAlarm: () -> Unit,
    onDelete: () -> Unit
) {
    val icon = when {
        reminder.title.contains("water", true) -> "💧"
        reminder.title.contains("workout", true) || reminder.title.contains("gym", true) -> "🏋️"
        reminder.title.contains("read", true) || reminder.title.contains("book", true) -> "📖"
        reminder.title.contains("call", true) -> "📞"
        reminder.title.contains("bill", true) || reminder.title.contains("emi", true) || reminder.title.contains("pay", true) -> "📄"
        reminder.title.contains("meditate", true) -> "🧘"
        else -> "🔔"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Palette.Surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Palette.TealSoft),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 20.sp)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = reminder.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Palette.Ink
                )
                
                val repeatLabel = if (reminder.rrule != null) " · " + reminder.rrule else ""
                Text(
                    text = Format.whenLabel(reminder.triggerAt, now, zone) + repeatLabel,
                    fontSize = 13.sp,
                    color = Palette.Muted
                )
            }

            TextButton(onClick = onTestAlarm) {
                Text("Alarm Test", fontSize = 11.sp, color = Palette.Teal)
            }

            Switch(
                checked = reminder.status == ReminderStatus.ACTIVE,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Palette.Surface,
                    checkedTrackColor = Palette.Teal,
                    uncheckedThumbColor = Palette.Muted,
                    uncheckedTrackColor = Palette.Paper
                )
            )
        }
    }
}
