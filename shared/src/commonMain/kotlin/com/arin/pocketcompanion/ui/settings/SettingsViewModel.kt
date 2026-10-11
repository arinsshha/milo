package com.arin.pocketcompanion.ui.settings

import androidx.lifecycle.ViewModel
import com.arin.pocketcompanion.data.repository.SettingsRepository
import com.arin.pocketcompanion.domain.MiloSettings
import com.arin.pocketcompanion.domain.PersonalityStyle
import com.arin.pocketcompanion.domain.VoiceEngine
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val voiceEngine: VoiceEngine? = null
) : ViewModel() {

    val settings: StateFlow<MiloSettings> = settingsRepository.settings

    fun selectAvatar(emoji: String) {
        settingsRepository.updateAvatar(emoji)
    }

    fun selectPersonality(style: PersonalityStyle) {
        settingsRepository.updatePersonality(style)
    }

    fun updateVoice(pitch: Float, speed: Float) {
        settingsRepository.updateVoice(pitch, speed)
        voiceEngine?.setVoiceParameters(pitch, speed)
    }

    fun toggleVoiceEnabled(enabled: Boolean) {
        settingsRepository.updateVoiceEnabled(enabled)
        if (!enabled) {
            voiceEngine?.stopSpeaking()
        }
    }

    fun testVoice() {
        val current = settings.value
        if (!current.voiceEnabled) return
        voiceEngine?.setVoiceParameters(current.voicePitch, current.voiceSpeed)
        val text = when (current.personality) {
            PersonalityStyle.PLAYFUL -> "Hey there! I'm Milo, your super companion! Ready for action?"
            PersonalityStyle.PROFESSIONAL -> "Hello. I am Milo, your pocket assistant. How may I assist you?"
            PersonalityStyle.GENTLE -> "Hi... I'm Milo. I'm here whenever you need a peaceful hand."
        }
        voiceEngine?.speak(text)
    }
}
