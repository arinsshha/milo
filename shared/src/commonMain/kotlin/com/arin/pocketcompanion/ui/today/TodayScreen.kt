package com.arin.pocketcompanion.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arin.pocketcompanion.ui.theme.DisplayFont
import com.arin.pocketcompanion.ui.theme.Palette
import org.koin.compose.viewmodel.koinViewModel

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment

import androidx.compose.material3.TextButton

@Composable
fun TodayScreen(
    name: String,
    onOpenMemory: () -> Unit = {},
    onOpenReminders: () -> Unit = {},
    onNewReminder: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    viewModel: TodayViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(state.dateLabel.uppercase(), color = Palette.Muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
                Text("${state.greeting}, $name", fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, color = Palette.Ink)
            }
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Palette.Surface)
                    .border(1.dp, Palette.Line, CircleShape)
                    .clickable(role = Role.Button, onClick = onOpenSettings),
                contentAlignment = Alignment.Center
            ) {
                Text("⚙️", fontSize = 20.sp)
            }
        }

        RightNowCard(state.rightNow)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Stat("Spent", state.monthSpent, "this month", Modifier.weight(1f))
            Stat("Remembered", state.memoriesCount.toString(), "things", Modifier.weight(1f), onClick = onOpenMemory)
            Stat("Coming up", state.timeline.size.toString(), "reminders", Modifier.weight(1f), onClick = onOpenReminders)
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Coming up", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Palette.Ink)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onOpenReminders) {
                        Text("View All", fontSize = 13.sp, color = Palette.Teal, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Palette.Teal)
                            .clickable { onNewReminder() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = Palette.Surface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(Modifier.card().padding(horizontal = 16.dp, vertical = 4.dp)) {
                if (state.timeline.isEmpty()) {
                    Text("Nothing scheduled. Tell Milo \"remind me to…\" to add something.",
                        color = Palette.Muted, fontSize = 14.sp, modifier = Modifier.padding(vertical = 14.dp))
                }
                state.timeline.forEach { item ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(item.time, Modifier.width(88.dp), color = Palette.Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Column {
                            Text(item.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Palette.Ink)
                            Text(item.sub, fontSize = 13.sp, color = Palette.Muted)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(96.dp)) // room for the floating Milo
    }
}

@Composable
private fun RightNowCard(rightNow: RightNow?) {
    Column(
        Modifier.fillMaxWidth().background(Palette.Teal, RoundedCornerShape(22.dp)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("RIGHT NOW", color = Palette.TealOnDark, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.7.sp)
        Text(rightNow?.title ?: "Nothing urgent", fontFamily = DisplayFont, fontSize = 22.sp, color = Palette.Surface)
        Text(rightNow?.detail ?: "Enjoy the quiet. Milo will speak up if something needs you.",
            fontSize = 14.sp, color = Palette.TealOnDark, lineHeight = 21.sp)
    }
}

@Composable
private fun Stat(label: String, value: String, sub: String, modifier: Modifier, onClick: (() -> Unit)? = null) {
    val tappable = if (onClick != null) Modifier.clip(RoundedCornerShape(16.dp)).clickable(role = Role.Button, onClick = onClick) else Modifier
    Column(modifier.then(tappable).card(16).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 12.sp, color = Palette.Muted, fontWeight = FontWeight.SemiBold)
        Text(value, fontFamily = DisplayFont, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Palette.Ink, maxLines = 1)
        Text(sub, fontSize = 12.sp, color = Palette.Muted)
    }
}

fun Modifier.card(radius: Int = 20): Modifier =
    this.fillMaxWidth()
        .background(Palette.Surface, RoundedCornerShape(radius.dp))
        .border(1.dp, Palette.Line, RoundedCornerShape(radius.dp))
