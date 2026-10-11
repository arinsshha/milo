package com.arin.pocketcompanion.ui.reminders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arin.pocketcompanion.common.Format
import com.arin.pocketcompanion.data.local.ReminderEntity
import com.arin.pocketcompanion.ui.components.Milo
import com.arin.pocketcompanion.ui.theme.DisplayFont
import com.arin.pocketcompanion.ui.theme.Palette
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun AlarmScreen(
    reminder: ReminderEntity,
    onGotIt: () -> Unit,
    onComplete: () -> Unit,
    onSnooze: (Int) -> Unit
) {
    val zone = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toEpochMilliseconds()
    var showSnoozeSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.Paper)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onGotIt) {
                    Text("✕", fontSize = 20.sp, color = Palette.Ink)
                }
            }

            // Milo Mascot Glow
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(Palette.TealSoft, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Milo(size = 80.dp)
                }

                Text(
                    text = "Reminder",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Palette.Teal,
                    letterSpacing = 1.sp
                )

                Text(
                    text = reminder.title,
                    fontFamily = DisplayFont,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Palette.Ink,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = Format.whenLabel(reminder.triggerAt, now, zone),
                    fontSize = 15.sp,
                    color = Palette.Muted
                )

                reminder.note?.let { noteText ->
                    Text(
                        text = "“$noteText”",
                        fontSize = 15.sp,
                        color = Palette.Ink,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onGotIt,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.Teal),
                    shape = CircleShape
                ) {
                    Text("Got it", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Palette.Surface)
                }

                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.Ink),
                    shape = CircleShape
                ) {
                    Text("Mark as Completed", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Palette.Surface)
                }

                OutlinedButton(
                    onClick = { showSnoozeSheet = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = CircleShape
                ) {
                    Text("Snooze", fontSize = 16.sp, color = Palette.Ink, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Snooze Sheet Overlay
        AnimatedVisibility(visible = showSnoozeSheet) {
            SnoozeSheet(
                onSelectSnooze = { minutes ->
                    showSnoozeSheet = false
                    onSnooze(minutes)
                },
                onCancel = { showSnoozeSheet = false }
            )
        }
    }
}

@Composable
fun SnoozeSheet(
    onSelectSnooze: (Int) -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Palette.Surface),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Snooze Reminder",
                fontFamily = DisplayFont,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Palette.Ink
            )
            Text(
                text = "When would you like Milo to remind you?",
                fontSize = 14.sp,
                color = Palette.Muted
            )

            val snoozeOptions = listOf(5 to "5 minutes", 15 to "15 minutes", 60 to "1 hour", 1440 to "Tomorrow")
            snoozeOptions.forEach { (mins, label) ->
                Button(
                    onClick = { onSelectSnooze(mins) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.Paper),
                    shape = CircleShape
                ) {
                    Text(label, color = Palette.Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", color = Palette.Muted, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun ReminderCompletedScreen(
    reminder: ReminderEntity,
    onDone: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.Paper)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Milo(size = 80.dp)
            Text("Reminder Completed", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Palette.Teal)
            Text(reminder.title, fontFamily = DisplayFont, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Palette.Ink)
            Text("“You've got this! Talk to you soon.”", fontSize = 15.sp, color = Palette.Muted, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Palette.Teal),
                shape = CircleShape
            ) {
                Text("Done", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Palette.Surface)
            }
        }
    }
}
