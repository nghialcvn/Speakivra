package com.nghialcvn.speakivra

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Approved cyan/blue/purple deep-glass visual language shared by all Neon screens. */
internal object NeonLook {
    val cyan = Color(0xFF00E7F9)
    val azure = Color(0xFF058BFF)
    val blue = Color(0xFF3055F3)
    val violet = Color(0xFF9E35F4)
    val lilac = Color(0xFFCD78FF)
    val panel = Color(0xEB102452)
    val deep = Color(0xF0122145)
    val text = Color(0xFFF6F8FF)
    val muted = Color(0xFFB6C5E7)
    val crest = Brush.horizontalGradient(listOf(cyan, azure, violet))
    val glossyPanel = Brush.linearGradient(listOf(
        Color(0xEF193B71), Color(0xF0162759), Color(0xF2191F55)
    ))
}

/** Use on a single entire card; no geometry changes to the approved layout. */
internal fun Modifier.neonGlass(radius: Int = 18, strong: Boolean = false): Modifier {
    val shape = RoundedCornerShape(radius.dp)
    return this.shadow(
        if (strong) 13.dp else 7.dp, shape,
        ambientColor = Color(0xAA0C5DE4),
        spotColor = if (strong) NeonLook.violet else NeonLook.azure
    ).clip(shape)
        .background(NeonLook.glossyPanel)
        .drawWithCache {
            val cyanGlow = Brush.radialGradient(listOf(Color(0x5000DFFF), Color.Transparent),
                center = Offset.Zero, radius = size.width * .62f)
            val violetGlow = Brush.radialGradient(listOf(Color(0x65A931FF), Color.Transparent),
                center = Offset(size.width, size.height), radius = size.width * .57f)
            val shine = Brush.verticalGradient(listOf(Color(0x35B3EDFF), Color.Transparent),
                endY = size.height * .25f)
            onDrawWithContent {
                drawRect(cyanGlow); drawRect(violetGlow); drawRect(shine)
                drawContent()
            }
        }
        .border(if (strong) 1.7.dp else 1.1.dp, NeonLook.crest, shape)
}

/** Transparent speech symbol is painted natively. No white-square icon overlay. */
@Composable
internal fun NeonBrandMark(modifier: Modifier = Modifier) {
    Image(painterResource(R.drawable.neon_brand_mark), "Logo Speakivra", modifier)
}

/** Sculpted headphones artwork without a pasted square or external image. */
@Composable
internal fun NeonHeadphones(modifier: Modifier = Modifier) {
    Image(painterResource(R.drawable.neon_headphones), null, modifier)
}

/** Metallic studio microphone pictured in the approved five-tab reference. */
@Composable
internal fun NeonVintageTabMicrophone(modifier: Modifier = Modifier) {
    Image(painterResource(R.drawable.neon_vintage_microphone), null, modifier)
}
