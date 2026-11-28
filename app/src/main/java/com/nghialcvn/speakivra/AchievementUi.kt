package com.nghialcvn.speakivra

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.*

/** Fit the complete sentence to its available width, including the user's font scale. */
@Composable
internal fun SingleLineInfo(text: String, modifier: Modifier = Modifier,
                            fontSize: TextUnit = 13.sp, color: Color = studio.ink,
                            fontWeight: FontWeight = FontWeight.Normal) {
    val measurer = rememberTextMeasurer()
    val base = LocalTextStyle.current.copy(color = color, fontWeight = fontWeight)
    BoxWithConstraints(modifier) {
        fun fits(value: Float) = measurer.measure(text, base.copy(fontSize = value.sp),
            softWrap = false, maxLines = 1).size.width + 2 <= constraints.maxWidth
        var fitted = fontSize.value
        if (!fits(fitted)) {
            var low = 1f
            var high = fitted
            repeat(10) {
                val middle = (low + high) / 2f
                if (fits(middle)) low = middle else high = middle
            }
            fitted = low
        }
        val style = base.copy(fontSize = fitted.sp)
        val measured = measurer.measure(text, style, softWrap = false, maxLines = 1)
        val width = with(LocalDensity.current) {
            (measured.size.width + 2).coerceAtMost(constraints.maxWidth).toDp()
        }
        Text(text, modifier = Modifier.width(width), style = style, maxLines = 1, softWrap = false)
    }
}

@Composable
internal fun AchievementSummary(total: Int, modifier: Modifier = Modifier,
                                fontSize: TextUnit = 13.sp,
                                fontWeight: FontWeight = FontWeight.Bold,
                                horizontalArrangement: Arrangement.Horizontal = Arrangement.Center) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement) {
        SingleLineInfo("Thành tích đã đạt: $total", Modifier.weight(1f, fill = false),
            fontSize = fontSize, fontWeight = fontWeight)
        Spacer(Modifier.width(5.dp))
        StudioIcon("diamond", Modifier.size(20.dp), studio.coral)
    }
}

@Composable
internal fun TopicDiamonds(label: String, earned: Int) {
    Row(Modifier.semantics { contentDescription = "$label: $earned trên 3 kim cương" },
        horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(3) { index ->
            StudioIcon("diamond", Modifier.size(17.dp),
                if (index < earned) studio.coral else Color(0xFF9CA5B5))
        }
    }
}
