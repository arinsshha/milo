package com.arin.pocketcompanion.data.repository

import com.arin.pocketcompanion.domain.MiloSettings
import com.arin.pocketcompanion.domain.PersonalityStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsRepository {
    private val _settings = MutableStateFlow(MiloSettings())
    val settings: StateFlow<MiloSettings> = _settings.asStateFlow()

    fun updateAvatar(emoji: String) {
        _settings.update { it.copy(avatarEmoji = emoji) }
    }

    fun updatePersonality(style: PersonalityStyle) {
        _settings.update { it.copy(personality = style) }
    }

    fun updateVoice(pitch: Float, speed: Float) {
        _settings.update { it.copy(voicePitch = pitch, voiceSpeed = speed) }
    }

    fun updateVoiceEnabled(enabled: Boolean) {
        _settings.update { it.copy(voiceEnabled = enabled) }
    }
}
