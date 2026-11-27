package com.nghialcvn.speakivra

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp

/** Approved Sáng/Tối brand treatments; the separate Neon effects live in NeonVisuals. */
internal object BrandLook {
    val cyan = Color(0xFF08D9DF)
    val violet = Color(0xFF7838EE)
    val action = Brush.horizontalGradient(listOf(Color(0xFF9634EF), Color(0xFF6022E0), Color(0xFF007DAF)))
    val selection = Brush.verticalGradient(listOf(Color(0xFF9A4DF7), Color(0xFF6130DE)))
    val progress = Brush.horizontalGradient(listOf(cyan, Color(0xFF1DA4F3), Color(0xFF9937F0)))
    val microphone = Brush.linearGradient(listOf(Color(0xFF59E7F5), Color(0xFF139FEE), Color(0xFF9140F3)))
}

internal fun brandPanelBrush(p: StudioColors, color: Color = p.card): Brush = Brush.linearGradient(
    if (p == DarkStudio) listOf(if (color == p.card) Color(0xFF2A3761) else color, color, Color(0xFF172141))
    else listOf(color, color, Color(0xFFF0F3FF)))

@Composable
internal fun Modifier.brandPanel(color: Color = studio.card, border: Boolean = true, radius: Int = 16): Modifier {
    val p = studio
    val shape = RoundedCornerShape(radius.dp)
    return this.shadow(3.dp, shape, ambientColor = Color(0x302C3A93), spotColor = Color(0x302C3A93))
        .clip(shape).background(brandPanelBrush(p, color))
        .then(if (border) Modifier.border(.8.dp, p.border, shape) else Modifier)
}

internal fun brandNavigationAccent(p: StudioColors): Color = when (p) {
    DarkStudio -> BrandLook.cyan
    else -> p.coral
}

@Composable
internal fun BrandFlame(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        scale(size.width / 24f, size.height / 30f, pivot = Offset.Zero) {
            val outer = Path().apply {
                moveTo(13f, 0f); cubicTo(14f, 9f, 20f, 9f, 19f, 17f)
                lineTo(22f, 12f); cubicTo(28f, 31f, -2f, 36f, 2f, 16f)
                lineTo(6f, 20f); cubicTo(3f, 10f, 13f, 10f, 13f, 0f); close()
            }
            drawPath(outer, Brush.verticalGradient(listOf(Color(0xFFAC44FF), Color(0xFF6120D8)), endY = 30f))
            val inner = Path().apply {
                moveTo(12f, 15f); cubicTo(21f, 25f, 12f, 32f, 8f, 26f)
                cubicTo(6f, 23f, 10f, 19f, 12f, 15f); close()
            }
            drawPath(inner, Color.White)
        }
    }
}

@Composable
internal fun BrandResultMark(passed: Boolean, modifier: Modifier = Modifier) {
    val p = studio
    Canvas(modifier) {
        if (passed) {
            drawCircle(Color(0xFF0BA757))
            val tick = Path().apply { moveTo(size.width * .25f, size.height * .52f); lineTo(size.width * .43f, size.height * .7f); lineTo(size.width * .76f, size.height * .32f) }
            drawPath(tick, Color.White, style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx(), cap = StrokeCap.Round))
        } else {
            drawLine(p.incorrectWord, Offset(size.width * .2f, size.height * .2f), Offset(size.width * .8f, size.height * .8f), 3.dp.toPx(), cap = StrokeCap.Round)
            drawLine(p.incorrectWord, Offset(size.width * .8f, size.height * .2f), Offset(size.width * .2f, size.height * .8f), 3.dp.toPx(), cap = StrokeCap.Round)
        }
    }
}
