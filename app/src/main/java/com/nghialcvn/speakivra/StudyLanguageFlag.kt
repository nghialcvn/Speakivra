package com.nghialcvn.speakivra

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** Flat circular flag artwork, independent of the device's emoji font. */
@Composable
internal fun StudyLanguageFlag(language: StudyLanguage, modifier: Modifier = Modifier) {
    Canvas(modifier.semantics { contentDescription = language.label }) {
        val circle = Path().apply { addOval(Rect(0f, 0f, size.width, size.height)) }
        fun star(x: Float, y: Float, radius: Float, angle: Double = -PI / 2): Path = Path().apply {
            repeat(10) { point ->
                val distance = radius * if (point % 2 == 0) 1f else .382f
                val a = angle + point * PI / 5
                val px = (x + cos(a) * distance).toFloat() * size.width
                val py = (y + sin(a) * distance).toFloat() * size.height
                if (point == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }
        clipPath(circle) {
            when (language) {
            StudyLanguage.ENGLISH -> {
                drawRect(Color.White)
                repeat(7) { stripe ->
                    drawRect(Color(0xFFB22234), topLeft = androidx.compose.ui.geometry.Offset(0f, size.height * stripe * 2 / 13),
                        size = androidx.compose.ui.geometry.Size(size.width, size.height / 13))
                }
                drawRect(Color(0xFF3C3B6E), size = androidx.compose.ui.geometry.Size(size.width * .5f, size.height * 7 / 13))
                repeat(9) { row ->
                    val count = if (row % 2 == 0) 6 else 5
                    repeat(count) { column ->
                        val x = (column + if (count == 6) .5f else 1f) / 6 * .5f
                        drawPath(star(x, (row + 1) / 10f * 7 / 13, .018f), Color.White)
                    }
                }
            }
            StudyLanguage.CHINESE -> {
                drawRect(Color(0xFFDE2910))
                drawPath(star(.28f, .34f, .16f), Color(0xFFFFDE00))
                listOf(.52f to .16f, .63f to .29f, .63f to .45f, .52f to .58f).forEach { (x, y) ->
                    drawPath(star(x, y, .055f, atan2((.34f - y).toDouble(), (.28f - x).toDouble())), Color(0xFFFFDE00))
                }
            }
            }
        }
    }
}
