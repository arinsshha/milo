package com.arin.pocketcompanion.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

enum class Glyph { TODAY, CHAT, MEMORY, FOCUS, MONEY }

/** Line icons matching the design, drawn on a 24-unit grid. */
@Composable
fun TabIcon(glyph: Glyph, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(22.dp)) {
        val u = size.minDimension / 24f
        val stroke = Stroke(width = 1.8f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun p(vararg pts: Pair<Float, Float>, close: Boolean = false) = Path().apply {
            pts.forEachIndexed { i, (x, y) -> if (i == 0) moveTo(x * u, y * u) else lineTo(x * u, y * u) }
            if (close) close()
        }
        fun DrawScope.line(path: Path) = drawPath(path, color, style = stroke)

        when (glyph) {
            Glyph.TODAY -> line(p(4f to 11f, 12f to 4f, 20f to 11f, 20f to 20f, 14f to 20f, 14f to 14f, 10f to 14f, 10f to 20f, 4f to 20f, close = true))
            Glyph.CHAT -> line(p(4f to 5f, 20f to 5f, 20f to 16f, 9f to 16f, 4f to 20f, close = true))
            Glyph.MEMORY -> line(p(6f to 3f, 18f to 3f, 18f to 21f, 12f to 17f, 6f to 21f, close = true))
            Glyph.FOCUS -> {
                drawCircle(color, radius = 8f * u, center = Offset(12f * u, 12f * u), style = stroke)
                drawCircle(color, radius = 3f * u, center = Offset(12f * u, 12f * u), style = stroke)
            }
            Glyph.MONEY -> {
                drawRoundRect(color, topLeft = Offset(3f * u, 6f * u), size = Size(18f * u, 13f * u),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * u), style = stroke)
                line(p(3f to 10f, 21f to 10f))
                line(p(16f to 14f, 18f to 14f))
            }
        }
    }
}
