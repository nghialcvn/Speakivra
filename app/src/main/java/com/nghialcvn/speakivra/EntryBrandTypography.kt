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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Only the entry wordmark changes; the learner picker keeps its approved lockup. */
@Composable
internal fun EntryBrandWordmark() {
    val fixed = LocalBrandSweepPhase.current
    val phase = if (fixed == null) {
        rememberInfiniteTransition(label = "Entry wordmark light").animateFloat(
            -.4f, 1.4f, infiniteRepeatable(keyframes {
                durationMillis = 6000
                -.4f at 0 using LinearEasing
                1.4f at 3600 using LinearEasing
                1.4f at 6000
            }), label = "Entry left to right sweep")
    } else rememberUpdatedState(fixed)
    val light = studio == LightStudio
    Box(Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.Center) {
        Image(painterResource(R.drawable.brand_wordmark), "Speakivra",
            Modifier.fillMaxWidth().height(64.dp).testTag("entry-brand-wordmark")
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

/** Shallow solid bevels, real Vietnamese text and one accessible label per card. */
@Composable
internal fun EntryLanguageTitle(label: String) {
    val light = studio == LightStudio
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val base = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = 0.sp)
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val natural = measurer.measure(label, style = base, maxLines = 1, softWrap = false)
        val room = with(density) { (maxWidth - 3.dp).toPx() }.coerceAtLeast(1f)
        var fit = minOf(1f, room / natural.size.width.coerceAtLeast(1))
        fun fittedStyle() = base.copy(fontSize = (32 * fit).sp, lineHeight = (40 * fit).sp)
        var layout = measurer.measure(label, style = fittedStyle(), maxLines = 1, softWrap = false)
        // Font hinting rounds at the final pixel size. A proportional estimate alone
        // can still overflow by a pixel, so measure the actual fitted glyphs as well.
        while (layout.size.width > room && fit > .05f) {
            fit = (fit - .01f).coerceAtLeast(.05f)
            layout = measurer.measure(label, style = fittedStyle(), maxLines = 1, softWrap = false)
        }
        val style = fittedStyle()
        val face = Brush.verticalGradient(if (light) listOf(Color(0xFF354876),
            Color(0xFF101744), Color(0xFF344A78)) else listOf(Color.White,
            Color(0xFFF2F5FF), Color(0xFFB9BDDB)), endY = layout.size.height.toFloat())
        Text(label, maxLines = 1, softWrap = false, style = style.copy(brush = face),
            // Use the allocated column width, rather than re-rounding the fitted
            // text's fractional intrinsic width into a tight, potentially clipped box.
            modifier = Modifier.fillMaxWidth().testTag("entry-title-$label").drawBehind {
                drawText(layout, color = Color(0xFF20133F), topLeft = Offset(1.dp.toPx(), 2.dp.toPx()))
                drawText(layout, color = Color(0xFF7141A8), topLeft = Offset(.5.dp.toPx(), 1.3.dp.toPx()))
                drawText(layout, color = Color(0xFF5DE5F0), topLeft = Offset(0f, (-.5).dp.toPx()))
            })
    }
}
