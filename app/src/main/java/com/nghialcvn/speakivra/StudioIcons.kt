package com.nghialcvn.speakivra

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/** Small native outline icons: no emoji glyphs, remote icon fonts or extra SDKs. */
private val Shapes = mapOf(
    "email" to "M2 5 L22 5 L22 20 L2 20 Z M2 5 L12 13 L22 5",
    "wifi" to "M2 8 Q12 -1 22 8 M5 12 Q12 5 19 12 M8 16 Q12 12 16 16 M12 20 L12 20.1",
    "check-circle" to "M12 2 C25 2 25 22 12 22 C-1 22 -1 2 12 2 Z M7 12 L11 16 L18 8",
    "cloud-check" to "M6 20 C-1 20 -1 10 6 9 C7 1 20 1 21 10 C28 14 23 21 19 20 Z M8 13 L11 16 L17 10",
    "cloud-sync" to "M6 18 C-1 18 -1 9 6 8 C7 0 20 0 21 9 C28 13 23 19 19 18 M8 17 L12 21 L16 17 M12 12 L12 21",
    "diamond" to "M2 8 L7 2 L17 2 L22 8 L12 22 Z M2 8 L22 8 M7 2 L9 8 L12 22 L15 8 L17 2 M9 8 L12 2 L15 8",
    "reception" to "M2 14 L22 14 L22 23 L2 23 Z M4 14 L4 10 Q4 7 8 7 Q12 7 12 10 L12 14 M8 1 C12 1 12 7 8 7 C4 7 4 1 8 1 Z M16 11 L23 11 M17 8 L22 8 L22 11 M19 5 L19 8",
    "housekeeping" to "M3 7 L20 7 L20 20 L3 20 Z M1 3 L3 3 L3 20 M5 20 L5 23 M18 20 L18 23 M8 7 L8 3 L11 3 L11 7 M14 7 L14 2 L18 2 L18 7 M3 14 L20 14",
    "door" to "M5 23 L5 2 L19 2 L19 23 M2 23 L22 23 M15 12 L15 14",
    "key" to "M10 10 C2 14 -1 3 6 2 C14 0 17 9 10 10 Z M10 10 L22 22 M18 18 L21 15 M15 15 L18 12",
    "towel" to "M5 3 L19 3 Q23 3 23 6 Q23 9 19 9 L5 9 Q1 9 1 6 Q1 3 5 3 Z M5 10 L19 10 Q23 10 23 13 Q23 16 19 16 L5 16 Q1 16 1 13 Q1 10 5 10 Z M5 17 L19 17 Q23 17 23 20 Q23 23 19 23 L5 23 Q1 23 1 20 Q1 17 5 17 Z",
    "laundry" to "M3 2 L22 2 L22 24 L3 24 Z M3 7 L22 7 M7 4 L8 4 M11 4 L12 4 M12 10 C21 10 21 22 12 22 C3 22 3 10 12 10 Z M8 16 Q12 12 16 16",
    "tools" to "M3 3 L8 8 L12 4 L11 1 Q23 3 18 12 L23 21 L20 24 L11 15 Q1 18 1 9 L4 10 L7 7 Z",
    "card" to "M2 5 L23 5 L23 21 L2 21 Z M2 10 L23 10 M6 16 L11 16",
    "sparkle" to "M12 1 L15 9 L23 12 L15 15 L12 23 L9 15 L1 12 L9 9 Z",
    "pencil" to "M4 16 L16 4 L20 8 L8 20 L3 21 Z M13 7 L17 11",
    "lock" to "M7 10 L7 7 C7 0 17 0 17 7 L17 10 M5 10 L19 10 L19 22 L5 22 Z M12 14 L12 18",
    "home" to "M3 10 L12 3 L21 10 L21 21 L15 21 L15 14 L9 14 L9 21 L3 21 Z",
    "mic" to "M9 6 C9 2 15 2 15 6 L15 12 C15 16 9 16 9 12 Z M6 11 L6 12 C6 20 18 20 18 12 L18 11 M12 19 L12 23 M9 23 L15 23",
    // Same thin outlined microphone as the five-tab approved mockup; no emoji and
    // distinct from the large round speak button inside the conversation screen.
    "tab-mic" to "M9 5 C9 1.8 15 1.8 15 5 L15 12 C15 16.2 9 16.2 9 12 Z M5 11 L5 12 C5 20.8 19 20.8 19 12 L19 11 M12 19 L12 23 M8 23 L16 23",
    "profile" to "M12 2 C19 2 19 11 12 11 C5 11 5 2 12 2 Z M2 23 C2 13 22 13 22 23",
    "down" to "M5 9 L12 16 L19 9",
    "chat" to "M5 4 L19 4 Q22 4 22 7 L22 15 Q22 18 19 18 L12 18 L6 22 L6 18 L5 18 Q2 18 2 15 L2 7 Q2 4 5 4 Z M7 11 L7.1 11 M12 11 L12.1 11 M17 11 L17.1 11",
    "bell" to "M5 17 L19 17 L17 13 L17 9 C17 2 7 2 7 9 L7 13 Z M10 21 Q12 23 14 21 M12 2 L12 1",
    "back" to "M20 12 L4 12 M10 6 L4 12 L10 18",
    "chevron" to "M9 5 L16 12 L9 19",
    "speaker" to "M3 9 L7 9 L12 5 L12 19 L7 15 L3 15 Z M16 8 Q20 12 16 16 M19 4 Q26 12 19 20",
    "layers" to "M2 8 L12 2 L22 8 L12 14 Z M2 13 L12 19 L22 13 M2 18 L12 24 L22 18",
    "bulb" to "M8 15 C1 8 8 3 12 3 C18 3 22 9 16 15 L16 18 L8 18 Z M9 21 L15 21 M12 0 L12 1 M2 3 L4 5 M0 11 L2 11 M22 11 L24 11 M20 4 L22 2",
    "sun" to "M12 6 C20 6 20 18 12 18 C4 18 4 6 12 6 Z M12 0 L12 3 M12 21 L12 24 M0 12 L3 12 M21 12 L24 12 M3 3 L5 5 M19 19 L21 21 M3 21 L5 19 M19 5 L21 3",
    "gauge" to "M3 20 C-3 7 6 0 12 2 C23 2 27 14 21 20 Z M12 14 L18 8 M6 10 L7 11 M12 6 L12 7 M18 16 L20 16 M5 16 L7 16",
    "target" to "M19 5 C11 -3 0 4 2 14 C4 25 22 24 22 12 M16 8 C7 2 1 16 10 19 C15 21 19 17 18 12 M12 12 L23 1 M19 1 L23 1 L23 5",
    "phone" to "M8 2 L16 2 Q18 2 18 4 L18 20 Q18 22 16 22 L8 22 Q6 22 6 20 L6 4 Q6 2 8 2 Z M10 19 L14 19",
    "check" to "M5 12 L10 17 L20 6",
    "close" to "M6 6 L18 18 M18 6 L6 18",
    "calendar" to "M5 4 L19 4 Q21 4 21 6 L21 21 L3 21 L3 6 Q3 4 5 4 Z M3 9 L21 9 M7 1 L7 6 M17 1 L17 6 M7 13 L8 13 M12 13 L13 13 M17 13 L18 13 M7 17 L8 17 M12 17 L13 17",
    "food" to "M3 2 L3 8 Q3 11 6 11 Q9 11 9 8 L9 2 M6 2 L6 23 M20 23 L20 2 Q13 3 14 13 L20 13",
    "bag" to "M4 7 L20 7 L22 23 L2 23 Z M8 7 L8 5 C8 -1 16 -1 16 5 L16 7",
    "map" to "M2 7 L8 4 L16 7 L22 4 L22 21 L16 24 L8 21 L2 24 Z M8 4 L8 21 M16 13 L16 24 M16 0 C8 0 12 9 16 13 C21 8 24 0 16 0 Z M16 4 L16 4.1",
    "family" to "M5 4 C5 0 11 0 11 4 C11 8 5 8 5 4 Z M16 5 C16 1 22 1 22 5 C22 9 16 9 16 5 Z M1 17 L1 12 Q1 9 5 9 M11 10 L14 10 M23 18 L23 13 Q23 10 20 10 M9 14 C9 10 15 10 15 14 C15 18 9 18 9 14 Z M6 24 L6 21 Q6 18 12 18 Q18 18 18 21 L18 24 M4 15 L4 22 M21 15 L21 22",
    "handshake" to "M1 8 L5 2 L10 5 L14 3 L22 8 L18 16 L14 21 L11 20 L8 21 L3 15 Z M5 5 L9 7 L7 10 Q7 12 10 11 L13 8 L20 14 M9 15 L14 20 M12 13 L17 18 M3 12 L1 10 M18 4 L23 7 L20 12",
    "book" to "M2 3 Q7 1 12 5 Q17 1 22 3 L22 21 Q17 19 12 23 Q7 19 2 21 Z M12 5 L12 23",
    "clock" to "M12 2 C25 2 25 22 12 22 C-1 22 -1 2 12 2 Z M12 6 L12 13 L17 16",
    "plane" to "M2 11 L10 9 L16 2 Q21 -1 19 5 L16 11 L23 16 L21 19 L14 15 L10 23 L7 23 L8 14 L2 14 Z",
    "globe" to "M12 2 C25 2 25 22 12 22 C-1 22 -1 2 12 2 Z M2 12 L22 12 M12 2 C5 7 5 17 12 22 M12 2 C19 7 19 17 12 22",
    "sliders" to "M4 5 L20 5 M4 12 L20 12 M4 19 L20 19 M9 2 L9 8 M16 9 L16 15 M10 16 L10 22",
    "volume" to "M3 9 L7 9 L12 5 L12 19 L7 15 L3 15 Z M15 9 Q19 12 15 15 M18 5 Q25 12 18 19",
    "info" to "M12 2 C25 2 25 22 12 22 C-1 22 -1 2 12 2 Z M12 10 L12 17 M12 6 L12 7",
    "keyboard" to "M2 5 L22 5 L22 19 L2 19 Z M5 9 L6 9 M9 9 L10 9 M13 9 L14 9 M17 9 L18 9 M5 13 L6 13 M9 13 L10 13 M13 13 L14 13 M17 13 L18 13 M7 16 L17 16",
    "next" to "M5 5 L14 12 L5 19 Z M18 4 L18 20",
    "settings" to "M12 3 C22 3 22 21 12 21 C2 21 2 3 12 3 Z M12 8 C17 8 17 16 12 16 C7 16 7 8 12 8 Z M12 0 L12 3 M12 21 L12 24 M0 12 L3 12 M21 12 L24 12 M4 4 L6 6 M18 18 L20 20 M20 4 L18 6 M6 18 L4 20",
    "progress" to "M3 20 L3 13 L7 13 L7 20 Z M10 20 L10 8 L14 8 L14 20 Z M17 20 L17 4 L21 4 L21 20 Z",
    "crown" to "M2 6 L7 11 L12 4 L17 11 L22 6 L20 20 L4 20 Z M5 23 L19 23"
)
@Composable
internal fun StudioIcon(name: String, modifier: Modifier = Modifier, color: Color = studio.ink) {
    val path = remember(name) { PathParser().parsePathString(Shapes[name] ?: Shapes.getValue("chat")).toPath() }
    Canvas(modifier.size(24.dp)) {
        scale(size.width / 26f, size.height / 26f, pivot = androidx.compose.ui.geometry.Offset.Zero) {
            drawPath(path, color, style = Stroke(if (name == "tab-mic") 1.9f else 1.65f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}
