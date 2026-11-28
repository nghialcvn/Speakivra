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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
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
        BrandSweepArtwork(R.drawable.brand_wordmark, "Speakivra",
            Modifier.fillMaxWidth().height(height).testTag(tag), phase,
            if (light) listOf(Color(0xFF101744), Color(0xFF253264), Color(0xFF101744))
            else listOf(Color.White, Color(0xFFDDE4F4), Color.White, Color(0xFFB8C5DD)))
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

/** Rasterize the small brand artwork before uploading it. This avoids emulator
 * GLES alpha-layer readback crashes while retaining the original transparent mask. */
@Composable
internal fun BrandSweepArtwork(resource: Int, description: String?, modifier: Modifier,
    phase: State<Float>, face: List<Color>? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val source = remember(resource) { android.graphics.BitmapFactory.decodeResource(context.resources, resource) }
    // Quantize the sweep, avoiding redundant bitmap uploads during the pause.
    val frame = (phase.value * 100).toInt()
    val bitmap = remember(source, frame, face) {
        val result = android.graphics.Bitmap.createBitmap(source.width, source.height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(result)
        canvas.drawBitmap(source, 0f, 0f, null)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_ATOP)
        }
        if (face != null) {
            paint.shader = android.graphics.LinearGradient(0f, 0f, 0f, source.height.toFloat(),
                face.map { it.toArgb() }.toIntArray(), null, android.graphics.Shader.TileMode.CLAMP)
            canvas.drawRect(0f, 0f, source.width.toFloat(), source.height.toFloat(), paint)
        }
        val center = source.width * frame / 100f
        val strength = if (face == null) .45f else .65f
        paint.shader = android.graphics.LinearGradient(center - source.width * .18f, 0f,
            center + source.width * .18f, 0f,
            listOf(Color.Transparent, Color(0xFF00D5E5).copy(alpha = strength),
                Color(0xFF70BFFF).copy(alpha = strength), Color(0xFFB75CFF).copy(alpha = strength),
                Color.Transparent).map { it.toArgb() }.toIntArray(), null, android.graphics.Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, source.width.toFloat(), source.height.toFloat(), paint)
        result.asImageBitmap()
    }
    Image(bitmap, description, modifier)
}

/** Scope alpha blending to the artwork without a persistent resized render layer. */
internal fun Modifier.brandMask(overlay: ContentDrawScope.() -> Unit): Modifier = drawWithContent {
    val canvas = drawContext.canvas
    canvas.saveLayer(Rect(Offset.Zero, size), Paint())
    try {
        drawContent()
        overlay()
    } finally {
        canvas.restore()
    }
}
