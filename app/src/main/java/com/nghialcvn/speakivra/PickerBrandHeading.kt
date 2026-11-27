package com.nghialcvn.speakivra

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.*

/** A fixed phase makes visual review deterministic; production uses the animated sweep. */
internal val LocalBrandSweepPhase = staticCompositionLocalOf<Float?> { null }

@Composable
internal fun PickerBrandHeading(modifier: Modifier = Modifier) {
    val ink = studio.ink
    val fixed = LocalBrandSweepPhase.current
    val animated = if (fixed == null) {
        val transition = rememberInfiniteTransition(label = "Speakivra wordmark shine")
        transition.animateFloat(-.4f, 1.4f,
            infiniteRepeatable(keyframes {
                durationMillis = 6000
                -.4f at 0 using LinearEasing
                1.4f at 3600 using LinearEasing
                1.4f at 6000
            }), label = "Left to right shine")
    } else rememberUpdatedState(fixed)
    val logo = ImageBitmap.imageResource(R.drawable.brand_logo_clear)
    Row(modifier.fillMaxWidth().height(80.dp), horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically) {
        // The approved symbol is cropped to its visible bounds: 64dp versus 32dp letters.
        Canvas(Modifier.size(82.53.dp, 64.dp).testTag("picker-brand-logo")
            .semantics { contentDescription = "Logo Speakivra" }) {
            drawImage(logo, srcOffset = IntOffset(50, 36), srcSize = IntSize(668, 518),
                dstSize = IntSize(size.width.toInt(), size.height.toInt()), filterQuality = FilterQuality.High)
        }
        Spacer(Modifier.width(8.dp))
        Image(painterResource(R.drawable.brand_wordmark), "Speakivra",
            Modifier.size(169.67.dp, 32.dp).testTag("picker-brand-wordmark")
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    val centre = size.width * animated.value
                    val half = size.width * .2f
                    drawRect(Brush.horizontalGradient(
                        listOf(Color.Transparent, Color(0xFF04CDD9), Color(0xFF3285FF),
                            Color(0xFFB75CFF), Color.Transparent),
                        startX = centre - half, endX = centre + half), blendMode = BlendMode.SrcAtop)
                }, colorFilter = ColorFilter.tint(ink))
    }
}
