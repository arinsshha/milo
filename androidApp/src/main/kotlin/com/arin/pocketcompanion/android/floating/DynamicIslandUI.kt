package com.arin.pocketcompanion.android.floating

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.TextButton
import com.arin.pocketcompanion.ui.theme.DisplayFont
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arin.pocketcompanion.data.repository.SettingsRepository
import com.arin.pocketcompanion.domain.ReelsCounterManager
import com.arin.pocketcompanion.ui.theme.Palette
import org.koin.compose.koinInject

@Composable
fun DynamicIslandUI(
    onCloseService: () -> Unit,
    onOpenMainApp: () -> Unit,
    settingsRepository: SettingsRepository = koinInject()
) {
    val isAppForeground by ReelsCounterManager.isReelsAppForeground.collectAsStateWithLifecycle()
    val reelsCount by ReelsCounterManager.todayReelsCount.collectAsStateWithLifecycle()
    val milestoneNudge by ReelsCounterManager.activeMilestone.collectAsStateWithLifecycle()
    val settings by settingsRepository.settings.collectAsStateWithLifecycle()

    var isExpandedManually by remember { mutableStateOf(false) }
    var showMutePopup by remember { mutableStateOf(false) }

    val now = System.currentTimeMillis()
    val isMuted = ReelsCounterManager.isMuted(now)

    // Reset manual expansion if Instagram/YouTube is closed
    LaunchedEffect(isAppForeground) {
        if (!isAppForeground) {
            isExpandedManually = false
            showMutePopup = false
        }
    }

    // Only render when Reels/Shorts is open AND not muted (or when milestone is active or manually triggered)
    if (!isMuted && (isAppForeground || milestoneNudge != null || isExpandedManually || showMutePopup)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp)
        ) {
            // Top Compact Dynamic Island Pill with Drag-Down to Remove
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            if (dragAmount.y > 15f) {
                                showMutePopup = true
                            }
                        }
                    }
                    .clickable { isExpandedManually = !isExpandedManually },
                color = Palette.Ink, // Sleek dark black island
                shadowElevation = 8.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = settings.avatarEmoji, fontSize = 16.sp)
                    Text(
                        text = "🎬 $reelsCount Reels",
                        color = Palette.Surface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Hide Mute Dialog Card (Triggered by dragging down)
            AnimatedVisibility(
                visible = showMutePopup,
                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit = scaleOut(spring(stiffness = Spring.StiffnessHigh)) + fadeOut(),
            ) {
                Card(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(0.92f)
                        .widthIn(max = 340.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Palette.Surface),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Hide Reel Tracker?",
                            fontFamily = DisplayFont,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Palette.Ink
                        )
                        Text(
                            text = "Milo will pause tracking and stay hidden for the selected time.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Palette.Muted
                        )
                        Button(
                            onClick = {
                                val currentNow = System.currentTimeMillis()
                                ReelsCounterManager.muteForHours(1, currentNow)
                                showMutePopup = false
                            },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Palette.Teal),
                            shape = CircleShape
                        ) {
                            Text("Hide for 1 Hour", color = Palette.Surface, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                val currentNow = System.currentTimeMillis()
                                ReelsCounterManager.muteForHours(5, currentNow)
                                showMutePopup = false
                            },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Palette.Teal),
                            shape = CircleShape
                        ) {
                            Text("Hide for 5 Hours", color = Palette.Surface, fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = { showMutePopup = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel", color = Palette.Muted, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Milestone Nudge Expansion (Every 100 Reels)
            AnimatedVisibility(
                visible = milestoneNudge != null && !showMutePopup,
                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit = scaleOut(spring(stiffness = Spring.StiffnessHigh)) + fadeOut(),
            ) {
                milestoneNudge?.let { nudge ->
                    Card(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(0.92f)
                            .widthIn(max = 340.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Palette.Surface),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = nudge.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Palette.Ink
                            )
                            Text(
                                text = nudge.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Palette.Muted
                            )
                            Button(
                                onClick = {
                                    ReelsCounterManager.dismissMilestone()
                                    onOpenMainApp()
                                },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Palette.Teal),
                                shape = CircleShape
                            ) {
                                Text("Back to Focus", color = Palette.Surface, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { ReelsCounterManager.dismissMilestone() },
                                    modifier = Modifier.weight(1f).heightIn(min = 40.dp),
                                    shape = CircleShape
                                ) {
                                    Text("5 More Min", fontSize = 11.sp, color = Palette.Ink)
                                }
                                OutlinedButton(
                                    onClick = { ReelsCounterManager.dismissMilestone() },
                                    modifier = Modifier.weight(1f).heightIn(min = 40.dp),
                                    shape = CircleShape
                                ) {
                                    Text("Keep Scrolling", fontSize = 11.sp, color = Palette.Muted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
