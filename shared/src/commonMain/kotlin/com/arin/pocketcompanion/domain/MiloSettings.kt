package com.arin.pocketcompanion.domain

enum class PersonalityStyle(val label: String, val promptContext: String) {
    PLAYFUL("Playful", "Be energetic, fun, encouraging, and use occasional emojis."),
    PROFESSIONAL("Professional", "Be concise, clear, polite, and direct."),
    GENTLE("Gentle", "Be warm, empathetic, patient, and soft-spoken.")
}

data class SuperheroAvatar(val emoji: String, val name: String)

val SUPERHERO_AVATARS = listOf(
    SuperheroAvatar("🦸‍♂️", "Hero"),
    SuperheroAvatar("🥷", "Ninja"),
    SuperheroAvatar("🧙‍♂️", "Wizard"),
    SuperheroAvatar("🦸‍♀️", "Heroine"),
    SuperheroAvatar("🦹‍♂️", "Anti-Hero"),
    SuperheroAvatar("🤖", "Mech"),
    SuperheroAvatar("⚡", "Flash"),
    SuperheroAvatar("👑", "Captain"),
    SuperheroAvatar("🚀", "Rocket"),
    SuperheroAvatar("💥", "Cosmic")
)

data class MiloSettings(
    val avatarEmoji: String = "🦸‍♂️",
    val personality: PersonalityStyle = PersonalityStyle.PLAYFUL,
    val voicePitch: Float = 1.0f,
    val voiceSpeed: Float = 1.0f,
    val voiceEnabled: Boolean = true
)
