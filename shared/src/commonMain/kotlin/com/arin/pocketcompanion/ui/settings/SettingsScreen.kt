package com.arin.pocketcompanion.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Surface
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
import com.arin.pocketcompanion.domain.PersonalityStyle
import com.arin.pocketcompanion.domain.SUPERHERO_AVATARS
import com.arin.pocketcompanion.ui.theme.DisplayFont
import com.arin.pocketcompanion.ui.theme.Palette
import org.koin.compose.koinInject

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinInject()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Milo Customization",
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

        // 1. Superhero Avatar Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Palette.Surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Superhero Avatar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Palette.Ink
                )
                Text(
                    text = "Choose Milo's superhero persona",
                    fontSize = 13.sp,
                    color = Palette.Muted
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(SUPERHERO_AVATARS, key = { it.name }) { avatar ->
                        val selected = settings.avatarEmoji == avatar.emoji
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selected) Palette.TealSoft else Palette.Paper)
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) Palette.Teal else Palette.Line,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { viewModel.selectAvatar(avatar.emoji) }
                                .padding(12.dp)
                        ) {
                            Text(text = avatar.emoji, fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = avatar.name,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) Palette.Teal else Palette.Ink
                            )
                        }
                    }
                }
            }
        }

        // 2. Personality Style Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Palette.Surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Personality Style",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Palette.Ink
                )

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PersonalityStyle.entries.forEach { style ->
                        FilterChip(
                            selected = settings.personality == style,
                            onClick = { viewModel.selectPersonality(style) },
                            label = { Text(style.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Palette.Paper,
                                labelColor = Palette.Muted,
                                selectedContainerColor = Palette.TealSoft,
                                selectedLabelColor = Palette.Teal,
                            )
                        )
                    }
                }
                
                Text(
                    text = settings.personality.promptContext,
                    fontSize = 13.sp,
                    color = Palette.Muted
                )
            }
        }

        // 3. Voice Pitch & Speed
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Palette.Surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Voice Settings",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Palette.Ink
                )

                // Voice Output Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Voice Output", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Palette.Ink)
                        Text(
                            text = if (settings.voiceEnabled) "Milo speaks replies out loud" else "Milo is muted (text only)",
                            fontSize = 12.sp,
                            color = Palette.Muted
                        )
                    }
                    Switch(
                        checked = settings.voiceEnabled,
                        onCheckedChange = { viewModel.toggleVoiceEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Palette.Surface,
                            checkedTrackColor = Palette.Teal,
                            uncheckedThumbColor = Palette.Muted,
                            uncheckedTrackColor = Palette.Paper
                        )
                    )
                }

                // Pitch
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Voice Pitch", fontSize = 14.sp, color = Palette.Ink)
                        Text("${(settings.voicePitch * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Palette.Teal)
                    }
                    Slider(
                        value = settings.voicePitch,
                        onValueChange = { viewModel.updateVoice(it, settings.voiceSpeed) },
                        valueRange = 0.5f..1.8f,
                        colors = SliderDefaults.colors(
                            thumbColor = Palette.Teal,
                            activeTrackColor = Palette.Teal
                        )
                    )
                }

                // Speed
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Speech Speed", fontSize = 14.sp, color = Palette.Ink)
                        Text("${(settings.voiceSpeed * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Palette.Teal)
                    }
                    Slider(
                        value = settings.voiceSpeed,
                        onValueChange = { viewModel.updateVoice(settings.voicePitch, it) },
                        valueRange = 0.5f..1.8f,
                        colors = SliderDefaults.colors(
                            thumbColor = Palette.Teal,
                            activeTrackColor = Palette.Teal
                        )
                    )
                }

                Button(
                    onClick = { viewModel.testVoice() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.Ink),
                    shape = CircleShape
                ) {
                    Text("🔊 Test Voice", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
