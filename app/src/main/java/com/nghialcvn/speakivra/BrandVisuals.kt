package com.nghialcvn.speakivra

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
