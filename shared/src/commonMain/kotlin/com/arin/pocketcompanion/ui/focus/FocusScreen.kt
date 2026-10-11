package com.arin.pocketcompanion.ui.focus

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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arin.pocketcompanion.domain.ReelsCounterManager
import com.arin.pocketcompanion.ui.components.Milo
import com.arin.pocketcompanion.ui.theme.DisplayFont
import com.arin.pocketcompanion.ui.theme.Palette
import com.arin.pocketcompanion.ui.today.card
import org.koin.compose.koinInject

@Composable
fun FocusScreen(
    onOpenAccessibilitySettings: (() -> Unit)? = null,
    viewModel: FocusViewModel = koinInject()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Focus Mode",
                fontFamily = DisplayFont,
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                color = Palette.Ink
            )
            Text(
                text = if (state.isActive) {
                    if (state.isBreak) "Taking a short break" else "Milo is keeping it quiet for you"
                } else {
                    "Build better habits, one step at a time"
                },
                color = Palette.Muted,
                fontSize = 14.sp
            )
        }

        // Center Clock UI
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            // Background ring
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = Palette.Line,
                strokeWidth = 12.dp
            )

            // Progress ring
            if (state.isActive) {
                CircularProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier.fillMaxSize(),
                    color = if (state.isBreak) Palette.Teal else Palette.Ink,
                    strokeWidth = 12.dp
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Milo(size = 44.dp, talking = false)
                
                val totalSeconds = state.remainingMillis / 1000
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                val timeString = if (state.isActive) {
                    "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
                } else {
                    "25:00"
                }

                Text(
                    text = timeString,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = Palette.Ink
                )

                Text(
                    text = when {
                        !state.isActive -> "READY"
                        state.isBreak -> "BREAK"
                        else -> "DEEP FOCUS"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Palette.Muted,
                    letterSpacing = 1.sp
                )
            }
        }

        // Action Buttons
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Palette.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!state.isActive) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.startFocusSession(25) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Palette.Ink),
                            shape = CircleShape
                        ) {
                            Text("25m Focus", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { viewModel.startBreak(5) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Palette.Teal),
                            shape = CircleShape
                        ) {
                            Text("5m Break", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { viewModel.endSession() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = CircleShape
                    ) {
                        Text("Stop Session", color = Palette.Ink, fontSize = 15.sp)
                    }
                }
            }
        }

        // Reels & Shorts Counter Card
        val reelsScrolled by ReelsCounterManager.todayReelsCount.collectAsStateWithLifecycle()
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Palette.Surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Reels & Shorts Tracker", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Palette.Ink)
                        Text("Nudges you every 50 reels", fontSize = 12.sp, color = Palette.Muted)
                    }
                    Text("🎬 $reelsScrolled", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Palette.Teal)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { ReelsCounterManager.incrementScroll() },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = CircleShape
                    ) {
                        Text("+1 Reel", fontSize = 12.sp, color = Palette.Ink)
                    }
                    OutlinedButton(
                        onClick = { ReelsCounterManager.resetCount() },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = CircleShape
                    ) {
                        Text("Reset", fontSize = 12.sp, color = Palette.Muted)
                    }
                }

                if (onOpenAccessibilitySettings != null) {
                    Button(
                        onClick = onOpenAccessibilitySettings,
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Palette.TealSoft),
                        shape = CircleShape
                    ) {
                        Text("Enable Auto-Detect Swipes ⚡", fontSize = 12.sp, color = Palette.Teal, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Habit & Routine Presets
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Presets & Routines", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Palette.Ink)

            val routines = listOf(
                Triple("☀️", "Morning Routine", "15 min · 1 session" to 15),
                Triple("📖", "Read & Learn", "20 min · 1 session" to 20),
                Triple("👟", "Move Your Body", "15 min · 1 session" to 15),
                Triple("🚫", "Unplug", "10 min · 1 session" to 10)
            )

            routines.forEach { (emoji, title, info) ->
                val (desc, duration) = info
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.startFocusSession(duration) },
                    colors = CardDefaults.cardColors(containerColor = Palette.Surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Palette.Paper),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 20.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Palette.Ink)
                            Text(desc, fontSize = 13.sp, color = Palette.Muted)
                        }
                        Text("›", fontSize = 20.sp, color = Palette.Muted)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
