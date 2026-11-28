package com.nghialcvn.speakivra

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.*

/** A fixed phase makes visual review deterministic; production uses the animated sweep. */
internal val LocalBrandSweepPhase = staticCompositionLocalOf<Float?> { null }

@Composable
internal fun PickerBrandHeading(modifier: Modifier = Modifier) {
    val logo = ImageBitmap.imageResource(R.drawable.brand_logo_clear)
    BoxWithConstraints(modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
        val scale = minOf(1f, maxWidth.value / 260.2f)
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Canvas(Modifier.size((82.53f * scale).dp, (64f * scale).dp).testTag("picker-brand-logo")
                .semantics { contentDescription = "Logo Speakivra" }) {
                drawImage(logo, srcOffset = IntOffset(50, 36), srcSize = IntSize(668, 518),
                    dstSize = IntSize(size.width.toInt(), size.height.toInt()), filterQuality = FilterQuality.High)
            }
            Spacer(Modifier.width((8f * scale).dp))
            EntryBrandWordmark(Modifier.width((169.67f * scale).dp), (32f * scale).dp, "picker-brand-wordmark")
        }
    }
}
