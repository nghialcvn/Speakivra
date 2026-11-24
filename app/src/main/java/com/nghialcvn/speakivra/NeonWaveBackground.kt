package com.nghialcvn.speakivra

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Approved full-screen cyan/blue/violet wave. Drawn behind the content AND
 * bottom navigation in a single full-root canvas, not a short scroll background.
 * It scales to any phone ratio, including landscape, without clipped wave ends.
 */
@Composable
internal fun NeonWaveBackground(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(
            brush = Brush.verticalGradient(listOf(
                Color(0xFF061124),
                Color(0xFF06132C),
                Color(0xFF071938),
                Color(0xFF071129)
            )),
            size = size
        )
        // Approved subtle upper ribbon: visually merges the transparent logo
        // into the same neon background instead of a pasted image rectangle.
        val topRibbon = Path().apply {
            moveTo(-w * .15f, h * .105f)
            cubicTo(w * .22f, h * .22f, w * .34f, h * .04f, w * .6f, h * .105f)
            cubicTo(w * .86f, h * .15f, w * .96f, h * .045f,
                w * 1.15f, h * .073f)
        }
        drawPath(topRibbon, brush = Brush.horizontalGradient(
            listOf(Color(0xFF00E6FD), Color(0xFF3267FF), Color(0xFFC266F9))),
            style = Stroke(width = 17f, cap = StrokeCap.Round), alpha = .12f)
        drawPath(topRibbon, brush = Brush.horizontalGradient(
            listOf(Color(0xFF00DDF9), Color(0xFF3C8FFF), Color(0xFFD17CFF))),
            style = Stroke(width = 2.2f, cap = StrokeCap.Round), alpha = .75f)

        // Soft glow extends to screen edges, even under Android navigation.
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0x77524AFD), Color(0x30424BBD), Color.Transparent),
                center = Offset(w * .97f, h * .82f), radius = w * 1.1f
            ),
            radius = w * 1.1f, center = Offset(w * .97f, h * .82f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0x55308FFF), Color(0x153478D2), Color.Transparent),
                center = Offset(w * .12f, h * .9f), radius = w * .83f
            ),
            radius = w * .83f, center = Offset(w * .12f, h * .9f)
        )

        val y1 = h * .78f
        val upper = Path().apply {
            moveTo(-w * .12f, y1 + h * .035f)
            cubicTo(w * .24f, y1 - h * .06f, w * .33f, y1 + h * .13f,
                w * .57f, y1 + h * .045f)
            cubicTo(w * .8f, y1 - h * .075f, w * .93f, y1 - h * .06f,
                w * 1.15f, y1 - h * .04f)
            lineTo(w * 1.15f, h * 1.1f)
            lineTo(-w * .12f, h * 1.1f)
            close()
        }
        drawPath(upper, brush = Brush.linearGradient(
            listOf(Color(0xE00078F6), Color(0xBA4457FF), Color(0xB98F36F0)),
            start = Offset(0f, y1), end = Offset(w, h)
        ))
        val crest = Path().apply {
            moveTo(-w * .12f, y1 + h * .035f)
            cubicTo(w * .24f, y1 - h * .06f, w * .33f, y1 + h * .13f,
                w * .57f, y1 + h * .045f)
            cubicTo(w * .8f, y1 - h * .075f, w * .93f, y1 - h * .06f,
                w * 1.15f, y1 - h * .04f)
        }
        drawPath(crest, brush = Brush.horizontalGradient(
            listOf(Color(0xFF00EEFF), Color(0xFF2D90FF), Color(0xFFD07AFF))
        ), style = Stroke(width = 16f, cap = StrokeCap.Round), alpha = .17f)
        drawPath(crest, brush = Brush.horizontalGradient(
            listOf(Color(0xFF00F0FF), Color(0xFF3C8FFF), Color(0xFFEE8FFF))
        ), style = Stroke(width = 3.5f, cap = StrokeCap.Round), alpha = .88f)

        val y2 = h * .89f
        val lower = Path().apply {
            moveTo(-w * .1f, y2 + h * .04f)
            cubicTo(w * .2f, y2 - h * .065f, w * .35f, y2 + h * .09f,
                w * .57f, y2 + h * .023f)
            cubicTo(w * .85f, y2 - h * .07f, w * 1.03f, y2 - h * .04f,
                w * 1.15f, y2 - h * .03f)
            lineTo(w * 1.15f, h * 1.15f)
            lineTo(-w * .1f, h * 1.15f)
            close()
        }
        drawPath(lower, brush = Brush.verticalGradient(
            listOf(Color(0xBB163DAB), Color(0xCC0D1B57), Color(0xFF080E2A)),
            startY = y2, endY = h
        ))
        val lowerCrest = Path().apply {
            moveTo(-w * .1f, y2 + h * .04f)
            cubicTo(w * .2f, y2 - h * .065f, w * .35f, y2 + h * .09f,
                w * .57f, y2 + h * .023f)
            cubicTo(w * .85f, y2 - h * .07f, w * 1.03f, y2 - h * .04f,
                w * 1.15f, y2 - h * .03f)
        }
        drawPath(lowerCrest,
            brush = Brush.horizontalGradient(listOf(
                Color(0xFF0378FC), Color(0xFF5B66FE), Color(0xFF874BFB))),
            style = Stroke(width = 2.5f, cap = StrokeCap.Round), alpha = .75f
        )
    }
}
