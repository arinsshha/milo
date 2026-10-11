package com.arin.pocketcompanion.ui.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arin.pocketcompanion.common.Format
import com.arin.pocketcompanion.data.local.ReminderEntity
import com.arin.pocketcompanion.domain.Priority
import com.arin.pocketcompanion.ui.theme.DisplayFont
import com.arin.pocketcompanion.ui.theme.Palette
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun CreateReminderScreen(
    editingReminder: ReminderEntity? = null,
    onBack: () -> Unit,
    onSave: (title: String, triggerAt: Long, rrule: String?, category: String, note: String?, ringtoneId: String, priority: Priority) -> Unit
) {
    val zone = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toEpochMilliseconds()

    var title by remember { mutableStateOf(editingReminder?.title ?: "") }
    var selectedCategory by remember { mutableStateOf(editingReminder?.category ?: "Personal") }
    var selectedRepeat by remember { mutableStateOf(editingReminder?.rrule ?: "none") }
    var selectedRingtone by remember { mutableStateOf(editingReminder?.ringtoneId ?: "Milo's Chime") }
    var note by remember { mutableStateOf(editingReminder?.note ?: "") }
    var isImportant by remember { mutableStateOf(editingReminder?.priority == Priority.IMPORTANT) }

    var selectedTriggerAt by remember { mutableStateOf(editingReminder?.triggerAt ?: (now + 3600 * 1000L)) }

    val categories = listOf("Personal", "Health", "Work", "Finance", "Habit", "Other")
    val ringtones = listOf("Milo's Chime", "Soft Bell", "Morning Birds", "Gentle Piano", "Classic Alarm")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (editingReminder != null) "Edit Reminder" else "Create Reminder",
                fontFamily = DisplayFont,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = Palette.Ink,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(Palette.Surface, CircleShape)
            ) {
                Text("✕", fontSize = 18.sp, color = Palette.Ink)
            }
        }

        // Title Input
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Palette.Surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("What should Milo remind you about?", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Palette.Ink)
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 120) title = it },
                    placeholder = { Text("Call Mom...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Palette.Paper,
                        unfocusedContainerColor = Palette.Paper,
                        focusedIndicatorColor = Palette.Paper,
                        unfocusedIndicatorColor = Palette.Paper
                    )
                )
                Text("${title.length}/120", fontSize = 11.sp, color = Palette.Muted, modifier = Modifier.align(Alignment.End))
            }
        }

        // Category Picker
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Palette.Surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Category", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Palette.Ink)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Palette.Paper,
                                labelColor = Palette.Muted,
                                selectedContainerColor = Palette.TealSoft,
                                selectedLabelColor = Palette.Teal,
                            )
                        )
                    }
                }
            }
        }

        // Date & Time Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Palette.Surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Schedule", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Palette.Ink)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Date & Time", fontSize = 14.sp, color = Palette.Muted)
                    Text(
                        text = Format.whenLabel(selectedTriggerAt, now, zone),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Palette.Teal
                    )
                }

                // Repeat Rule
                Text("Repeat", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Palette.Ink)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val repeatOptions = listOf("none" to "Does not repeat", "FREQ=DAILY" to "Daily", "FREQ=WEEKLY" to "Weekly", "FREQ=MONTHLY" to "Monthly")
                    repeatOptions.forEach { (rule, label) ->
                        FilterChip(
                            selected = selectedRepeat == rule,
                            onClick = { selectedRepeat = rule },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Palette.Paper,
                                labelColor = Palette.Muted,
                                selectedContainerColor = Palette.TealSoft,
                                selectedLabelColor = Palette.Teal,
                            )
                        )
                    }
                }
            }
        }

        // Ringtone Picker
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Palette.Surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Notification Ringtone", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Palette.Ink)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ringtones.forEach { r ->
                        FilterChip(
                            selected = selectedRingtone == r,
                            onClick = { selectedRingtone = r },
                            label = { Text(r) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Palette.Paper,
                                labelColor = Palette.Muted,
                                selectedContainerColor = Palette.TealSoft,
                                selectedLabelColor = Palette.Teal,
                            )
                        )
                    }
                }
            }
        }

        // Optional Note
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Palette.Surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Add a note (optional)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Palette.Ink)
                OutlinedTextField(
                    value = note,
                    onValueChange = { if (it.length <= 500) note = it },
                    placeholder = { Text("e.g. Ask how her appointment went...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Palette.Paper,
                        unfocusedContainerColor = Palette.Paper,
                        focusedIndicatorColor = Palette.Paper,
                        unfocusedIndicatorColor = Palette.Paper
                    )
                )
                Text("${note.length}/500", fontSize = 11.sp, color = Palette.Muted, modifier = Modifier.align(Alignment.End))
            }
        }

        // Save Button
        Button(
            onClick = {
                if (title.isNotBlank()) {
                    onSave(
                        title,
                        selectedTriggerAt,
                        selectedRepeat.takeIf { it != "none" },
                        selectedCategory,
                        note.takeIf { it.isNotBlank() },
                        selectedRingtone,
                        if (isImportant) Priority.IMPORTANT else Priority.NORMAL
                    )
                }
            },
            enabled = title.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Palette.Teal),
            shape = CircleShape
        ) {
            Text("Save Reminder", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Palette.Surface)
        }
    }
}
