package com.arin.pocketcompanion.android.floating

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arin.pocketcompanion.android.voice.AndroidVoiceEngine
import com.arin.pocketcompanion.ui.chat.ChatViewModel
import com.arin.pocketcompanion.ui.chat.ChatItem
import org.koin.compose.koinInject
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.LaunchedEffect

import com.arin.pocketcompanion.domain.ReelsCounterManager
import com.arin.pocketcompanion.data.repository.SettingsRepository

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import com.arin.pocketcompanion.ui.theme.Palette

@Composable
fun FloatingMiloUI(
    onClose: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onSizeChanged: (Int, Int) -> Unit,
    onExpandChange: (Boolean) -> Unit,
    viewModel: ChatViewModel = koinInject(),
    voiceEngine: AndroidVoiceEngine = koinInject(),
    settingsRepository: SettingsRepository = koinInject()
) {
    var expanded by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings by settingsRepository.settings.collectAsStateWithLifecycle()
    val milestoneNudge by ReelsCounterManager.activeMilestone.collectAsStateWithLifecycle()
    val reelsScrolled by ReelsCounterManager.todayReelsCount.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(state.items.size) {
        if (state.items.isNotEmpty()) listState.animateScrollToItem(state.items.lastIndex)
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .onSizeChanged { size ->
                onSizeChanged(size.width, size.height)
            },
        horizontalAlignment = Alignment.End
    ) {
        // Floating Bubble (Always visible)
        Surface(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    }
                },
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 8.dp,
            onClick = { 
                expanded = !expanded
                onExpandChange(expanded)
            }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = settings.avatarEmoji,
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }

        // Milestone Nudge Overlay (Appears automatically every 50 Reels)
        AnimatedVisibility(
            visible = milestoneNudge != null,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = scaleOut(spring(stiffness = Spring.StiffnessHigh)) + fadeOut(),
        ) {
            milestoneNudge?.let { nudge ->
                Card(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .widthIn(max = 300.dp),
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
                            onClick = { ReelsCounterManager.dismissMilestone() },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Palette.Teal),
                            shape = CircleShape
                        ) {
                            Text(nudge.primaryButton, color = Palette.Surface, fontWeight = FontWeight.Bold)
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
                                Text(nudge.secondaryButton, fontSize = 11.sp, color = Palette.Ink)
                            }
                            OutlinedButton(
                                onClick = { ReelsCounterManager.dismissMilestone() },
                                modifier = Modifier.weight(1f).heightIn(min = 40.dp),
                                shape = CircleShape
                            ) {
                                Text(nudge.tertiaryButton, fontSize = 11.sp, color = Palette.Muted)
                            }
                        }
                    }
                }
            }
        }

        // Expanded Chat Window
        AnimatedVisibility(
            visible = expanded && milestoneNudge == null,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = scaleOut(spring(stiffness = Spring.StiffnessHigh)) + fadeOut(),
        ) {
            Card(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .widthIn(max = 300.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Chat ($reelsScrolled Reels)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { ReelsCounterManager.incrementScroll() }) {
                            Text("🎬+", fontSize = 14.sp)
                        }
                        IconButton(onClick = { 
                            expanded = false
                            onExpandChange(false)
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Floating Window")
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .padding(vertical = 8.dp)
                    ) {
                        if (state.items.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "Hi! I'm Milo, your pocket companion. How can I help you right now?",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        items(state.items, key = { it.id }) { item ->
                            val (text, isUser, bgColor, textColor) = when (item) {
                                is ChatItem.User -> listOf(item.text, true, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
                                is ChatItem.Milo -> listOf(item.text, false, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                                is ChatItem.Action -> listOf(item.result.title, false, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                                is ChatItem.Confirm -> listOf(item.question, false, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                contentAlignment = if (isUser as Boolean) Alignment.CenterEnd else Alignment.CenterStart
                            ) {
                                Box(
                                    modifier = Modifier
                                        .widthIn(max = 240.dp)
                                        .background(
                                            bgColor as Color,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = text as String,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = textColor as Color
                                    )
                                }
                            }
                        }
                        
                        if (state.sending) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = "Thinking...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    var text by remember { mutableStateOf("") }
                    var isListening by remember { mutableStateOf(false) }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            placeholder = { Text(if (isListening) "Listening..." else "Ask Milo...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = {
                                isListening = true
                                voiceEngine.startListening(
                                    onResult = { spokenText ->
                                        isListening = false
                                        text = spokenText
                                        if (spokenText.isNotBlank()) {
                                            viewModel.send(spokenText)
                                            text = ""
                                        }
                                    },
                                    onError = {
                                        isListening = false
                                    }
                                )
                            },
                            modifier = Modifier
                                .background(if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                        ) {
                            Text("🎤", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { 
                                if (text.isNotBlank()) {
                                    viewModel.send(text)
                                    text = ""
                                }
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
