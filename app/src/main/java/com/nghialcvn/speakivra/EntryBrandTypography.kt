package com.nghialcvn.speakivra

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Only the entry wordmark changes; the learner picker keeps its approved lockup. */
@Composable
internal fun EntryBrandWordmark(modifier: Modifier = Modifier, height: Dp = 64.dp, tag: String = "entry-brand-wordmark", sweep: State<Float>? = null) {
    val phase = sweep ?: rememberBrandSweepPhase()
    val light = studio == LightStudio
    Box(modifier.fillMaxWidth().height(height), contentAlignment = Alignment.Center) {
        for (depth in 3 downTo 1) {
            Image(painterResource(R.drawable.brand_wordmark), null,
                Modifier.fillMaxWidth().height(height).offset(x = (-depth * .5f).dp, y = depth.dp),
                colorFilter = ColorFilter.tint(if (depth == 3) Color(0xFF32116A) else Color(0xFF6022D8)))
        }
        Image(painterResource(R.drawable.brand_wordmark), "Speakivra",
            Modifier.fillMaxWidth().height(height).testTag(tag)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawRect(Brush.verticalGradient(if (light) listOf(
                        Color(0xFF101744), Color(0xFF253264), Color(0xFF101744)) else listOf(
                        Color.White, Color(0xFFDDE4F4), Color.White, Color(0xFFB8C5DD))),
                        blendMode = BlendMode.SrcAtop)
                    val center = size.width * phase.value
                    drawRect(Brush.horizontalGradient(listOf(Color.Transparent,
                        Color(0xFF00D5E5).copy(alpha = .7f), Color(0xFF70BFFF).copy(alpha = .6f),
                        Color(0xFFB75CFF).copy(alpha = .65f), Color.Transparent),
                        startX = center - size.width * .18f, endX = center + size.width * .18f),
                        blendMode = BlendMode.SrcAtop)
                })
    }
}

@Composable
internal fun rememberBrandSweepPhase(cycleMillis: Int = 6000): State<Float> {
    val fixed = LocalBrandSweepPhase.current
    return if (fixed == null) {
        rememberInfiniteTransition(label = "Entry wordmark light").animateFloat(
            -.4f, 1.4f, infiniteRepeatable(keyframes {
                durationMillis = cycleMillis
                -.4f at 0 using LinearEasing
                1.4f at (cycleMillis * .6f).toInt() using LinearEasing
                1.4f at cycleMillis
            }), label = "Entry left to right sweep")
    } else rememberUpdatedState(fixed)
}

internal fun Modifier.brandShine(phase: State<Float>): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }.drawWithContent {
        drawContent()
        val center = size.width * phase.value
        drawRect(Brush.horizontalGradient(listOf(Color.Transparent, Color(0xFF00D5E5).copy(alpha = .45f),
            Color(0xFF70BFFF).copy(alpha = .45f), Color(0xFFB75CFF).copy(alpha = .45f), Color.Transparent),
            startX = center - size.width * .18f, endX = center + size.width * .18f), blendMode = BlendMode.SrcAtop)
    }
