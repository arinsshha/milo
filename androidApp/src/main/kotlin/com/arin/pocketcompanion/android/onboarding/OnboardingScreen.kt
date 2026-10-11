package com.arin.pocketcompanion.android.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arin.pocketcompanion.ui.components.Milo
import com.arin.pocketcompanion.ui.theme.DisplayFont
import com.arin.pocketcompanion.ui.theme.Palette

private data class Step(
    val title: String,
    val body: String,
    val primaryLabel: String,
    val secondaryLabel: String?,
    val note: String,
)

private val steps = listOf(
    Step(
        title = "Hey, I'm Milo",
        body = "I'll remember the things you tell me, remind you at the right time, and help you get back on track without nagging.",
        primaryLabel = "Let's go",
        secondaryLabel = null,
        note = "I need a couple of permissions to actually do this — not to watch you, just to remind and notify you. I'll explain each one before asking.",
    ),
    Step(
        title = "Let me notify you",
        body = "Without this, reminders like \"pay EMI on the 5th\" go off silently and you'll never see them.",
        primaryLabel = "Allow notifications",
        secondaryLabel = "Skip for now",
        note = "Used for reminders and gentle nudges. Never used for ads.",
    ),
    Step(
        title = "Let me set exact alarms",
        body = "Android can delay reminders by several minutes to save battery. This keeps \"remind me at 9:00\" actually meaning 9:00.",
        primaryLabel = "Open settings",
        secondaryLabel = "Skip for now",
        note = "You'll see an Android settings screen next. Find Pocket Companion and turn the toggle on, then come back here.",
    ),
    Step(
        title = "You're set",
        body = "Try saying \"remind me to drink water in 2 minutes\" and I'll show you it works.",
        primaryLabel = "Start using Milo",
        secondaryLabel = null,
        note = "You can change any of this later in Settings → Milo's permissions.",
    ),
)

@Composable
fun OnboardingScreen(
    onRequestNotifications: () -> Unit,
    onOpenAlarmSettings: () -> Unit,
    onFinish: () -> Unit,
) {
    var index by remember { mutableIntStateOf(0) }
    val step = steps[index]

    fun advance() {
        if (index == steps.lastIndex) onFinish() else index++
    }

    Box(Modifier.fillMaxSize().background(Palette.Paper).statusBarsPadding().navigationBarsPadding()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Dots(index, steps.size, Modifier.padding(top = 8.dp, bottom = 28.dp))

            Column(
                Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                StepArt(index)
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(step.title, fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, color = Palette.Ink)
                    Text(step.body, fontSize = 15.sp, color = Palette.Muted, lineHeight = 22.sp, textAlign = TextAlign.Center)
                }
                Column(
                    Modifier.fillMaxWidth()
                        .background(Palette.Surface, RoundedCornerShape(20.dp))
                        .border(1.dp, Palette.Line, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                ) {
                    Text(step.note, fontSize = 13.sp, color = Palette.Muted, lineHeight = 19.sp)
                }
            }

            Column(Modifier.padding(top = 20.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        when (index) {
                            1 -> onRequestNotifications()
                            2 -> onOpenAlarmSettings()
                        }
                        advance()
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.Teal),
                ) { Text(step.primaryLabel, fontSize = 16.sp, fontWeight = FontWeight.Bold) }

                if (step.secondaryLabel != null) {
                    TextButton(onClick = { advance() }, modifier = Modifier.fillMaxWidth().height(44.dp)) {
                        Text(step.secondaryLabel, color = Palette.Muted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun Dots(current: Int, total: Int, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth()) {
        Row(
            Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(total) { i ->
                val on = i == current
                Box(
                    Modifier.height(8.dp)
                        .width(if (on) 22.dp else 8.dp)
                        .background(if (on) Palette.Teal else Palette.Line, RoundedCornerShape(50)),
                )
            }
        }
    }
}

@Composable
private fun StepArt(index: Int) {
    when (index) {
        0 -> IconCircle(Color.Transparent, size = 120.dp) { Milo(size = 88.dp) }
        1 -> IconCircle(Palette.TealSoft) { Text("🔔", fontSize = 40.sp) }
        2 -> IconCircle(Color(0xFFF8DFCB)) { Text("⏰", fontSize = 40.sp) }
        3 -> IconCircle(Palette.Teal, size = 96.dp) { Text("✓", fontSize = 36.sp, color = Color.White, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun IconCircle(bg: Color, size: Dp = 96.dp, content: @Composable () -> Unit) {
    Box(
        Modifier.size(size).background(bg, CircleShape),
        contentAlignment = Alignment.Center,
    ) { content() }
}