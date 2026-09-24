package com.arin.pocketcompanion.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arin.pocketcompanion.ui.theme.Palette

/**
 * Milo, drawn in code so he scales perfectly and can animate.
 * While [talking], his mouth flaps in an uneven rhythm, the same one as the design canvas.
 * Later: drive this from TextToSpeech's onRangeStart so it syncs to real words.
 */
@Composable
fun Milo(size: Dp = 56.dp, talking: Boolean = false, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "milo")
    val mouthOpen by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 700
                0.15f at 0
                0.9f at 120
                0.35f at 240
                1f at 380
                0.25f at 520
                0.7f at 610
                0.15f at 700
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "mouth",
    )

    Canvas(modifier.size(size)) {
        val r = this.size.minDimension / 2f
        val c = center

        drawCircle(Palette.MiloBody, radius = r * 0.93f, center = c)
        drawCircle(Palette.MiloOutline, radius = r * 0.93f, center = c, style = Stroke(width = r * 0.07f))

        val eyeW = r * 0.22f
        val eyeH = r * 0.29f
        val eyeY = c.y - r * 0.12f - eyeH / 2
        drawOval(Palette.Ink, topLeft = Offset(c.x - r * 0.25f - eyeW / 2, eyeY), size = Size(eyeW, eyeH))
        drawOval(Palette.Ink, topLeft = Offset(c.x + r * 0.25f - eyeW / 2, eyeY), size = Size(eyeW, eyeH))

        if (talking) {
            val w = r * 0.42f
            val h = r * 0.38f * mouthOpen.coerceAtLeast(0.12f)
            drawOval(Palette.Ink, topLeft = Offset(c.x - w / 2, c.y + r * 0.3f - h / 2), size = Size(w, h))
        } else {
            val smile = Path().apply {
                moveTo(c.x - r * 0.25f, c.y + r * 0.22f)
                quadraticTo(c.x, c.y + r * 0.5f, c.x + r * 0.25f, c.y + r * 0.22f)
            }
            drawPath(smile, Palette.Ink, style = Stroke(width = r * 0.09f, cap = StrokeCap.Round))
        }
    }
}
