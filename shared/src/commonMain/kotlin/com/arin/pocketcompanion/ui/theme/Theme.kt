package com.arin.pocketcompanion.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

/** Colors from the approved design canvas. */
object Palette {
    val Paper = Color(0xFFF5F1E8)
    val Surface = Color(0xFFFFFDF8)
    val Ink = Color(0xFF1C1A17)
    val Muted = Color(0xFF5F5A52)
    val Line = Color(0xFFE4DDD0)
    val Teal = Color(0xFF235E53)
    val TealSoft = Color(0xFFDCE9E3)
    val TealOnDark = Color(0xFFE6F0EC)
    val MiloBody = Color(0xFFEE9A5E)
    val MiloOutline = Color(0xFFC8662F)
}

// TODO: bundle Fraunces + Manrope with Compose resources. System serif/sans until then.
val DisplayFont: FontFamily = FontFamily.Serif
val BodyFont: FontFamily = FontFamily.SansSerif

@Composable
fun CompanionTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Palette.Teal,
            onPrimary = Color.White,
            background = Palette.Paper,
            onBackground = Palette.Ink,
            surface = Palette.Surface,
            onSurface = Palette.Ink,
            outline = Palette.Line,
        ),
        content = content,
    )
}
