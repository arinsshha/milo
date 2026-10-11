package com.arin.pocketcompanion.domain

interface VoiceEngine {
    fun speak(text: String)
    fun stopSpeaking()
    fun setVoiceParameters(pitch: Float, speed: Float) {}
}
