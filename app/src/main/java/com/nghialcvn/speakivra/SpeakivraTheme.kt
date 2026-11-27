package com.nghialcvn.speakivra

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight

internal data class StudioColors(
    val background: Color, val card: Color, val ink: Color, val muted: Color,
    val border: Color, val peach: Color, val lavender: Color, val mint: Color,
    val coral: Color = Color(0xFFCE4D39), val coralLight: Color = Color(0xFFF1846D),
    val green: Color, val purple: Color,
    val correctWord: Color = green, val incorrectWord: Color = coral
)
internal val LightStudio = StudioColors(
    background = Color(0xFFF5F7FF), card = Color(0xFFFCFDFF), ink = Color(0xFF101744),
    muted = Color(0xFF505B79), border = Color(0xFFDADFF3), peach = Color(0xFFEDE5FF),
    lavender = Color(0xFFF1EDFF), mint = Color(0xFFE1F8EC),
    coral = Color(0xFF6022D8), coralLight = Color(0xFF9451F4),
    green = Color(0xFF176B3B), purple = Color(0xFF6022D8),
    correctWord = Color(0xFF137338), incorrectWord = Color(0xFFCE252E))
internal val DarkStudio = StudioColors(
    background = Color(0xFF090F29), card = Color(0xFF182344), ink = Color(0xFFF7F9FF),
    muted = Color(0xFFC1CCE9), border = Color(0xFF46557F), peach = Color(0xFF2D285C),
    lavender = Color(0xFF202C50), mint = Color(0xFF153D36),
    coral = Color(0xFFBFA7FF), coralLight = Color(0xFF9D74FF),
    green = Color(0xFF81EEB0), purple = Color(0xFFC8B5FF),
    correctWord = Color(0xFF80E99F), incorrectWord = Color(0xFFFF777F))
/** The separate Neon appearance keeps its approved palette and effects. */
internal val NeonStudio = StudioColors(
    background = Color(0xFF061332),
    card = Color(0xEA112453),
    ink = Color(0xFFF7F9FF),
    muted = Color(0xFFB8C8EB),
    border = Color(0xFF426DBA),
    peach = Color(0xDA173E79),
    lavender = Color(0xE8172D6B),
    mint = Color(0xEC07506D),
    coral = Color(0xFF00DEFA),
    coralLight = Color(0xFF2C87FF),
    green = Color(0xFF62FFE2),
    purple = Color(0xFFC77AFF),
    correctWord = Color(0xFF62F59A),
    incorrectWord = Color(0xFFFF7187)
)
internal val LocalStudioColors = staticCompositionLocalOf { LightStudio }
internal val studio: StudioColors @Composable get() = LocalStudioColors.current

@OptIn(ExperimentalTextApi::class)
private val Nunito = FontFamily(listOf(400, 600, 700, 800).map { weight ->
    Font(R.font.nunito, weight = FontWeight(weight), variationSettings = FontVariation.Settings(FontVariation.weight(weight)))
})
private val BaseTypography = Typography()
private val StudioTypography = Typography(
    displayLarge = BaseTypography.displayLarge.copy(fontFamily = Nunito),
    displayMedium = BaseTypography.displayMedium.copy(fontFamily = Nunito),
    displaySmall = BaseTypography.displaySmall.copy(fontFamily = Nunito),
    headlineLarge = BaseTypography.headlineLarge.copy(fontFamily = Nunito),
    headlineMedium = BaseTypography.headlineMedium.copy(fontFamily = Nunito),
    headlineSmall = BaseTypography.headlineSmall.copy(fontFamily = Nunito),
    titleLarge = BaseTypography.titleLarge.copy(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold),
    titleMedium = BaseTypography.titleMedium.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold),
    titleSmall = BaseTypography.titleSmall.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold),
    bodyLarge = BaseTypography.bodyLarge.copy(fontFamily = Nunito),
    bodyMedium = BaseTypography.bodyMedium.copy(fontFamily = Nunito),
    bodySmall = BaseTypography.bodySmall.copy(fontFamily = Nunito),
    labelLarge = BaseTypography.labelLarge.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold),
    labelMedium = BaseTypography.labelMedium.copy(fontFamily = Nunito),
    labelSmall = BaseTypography.labelSmall.copy(fontFamily = Nunito)
)
@Composable
internal fun SpeakivraTheme(dark: Boolean, neon: Boolean = false, content: @Composable () -> Unit) {
    val p = when {
        neon -> NeonStudio
        dark -> DarkStudio
        else -> LightStudio
    }
    val scheme = if (dark || neon) darkColorScheme(primary = p.coral, onPrimary = p.background,
        secondary = p.green, background = p.background, onBackground = p.ink, surface = p.card,
        onSurface = p.ink, onSurfaceVariant = p.muted, outline = p.border)
    else lightColorScheme(primary = p.coral, onPrimary = Color.White, secondary = p.green,
        background = p.background, onBackground = p.ink, surface = p.card, onSurface = p.ink,
        onSurfaceVariant = p.muted, outline = p.border)
    CompositionLocalProvider(LocalStudioColors provides p) {
        MaterialTheme(colorScheme = scheme, typography = StudioTypography, content = content)
    }
}
