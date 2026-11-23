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
    val green: Color, val purple: Color
)
internal val LightStudio = StudioColors(Color(0xFFFFF9F4), Color.White, Color(0xFF201C2B),
    Color(0xFF655E6B), Color(0xFFE8DEE0), Color(0xFFFFE5DA), Color(0xFFF0E8FF),
    Color(0xFFE0F6E9), green = Color(0xFF20723F), purple = Color(0xFF7560B0))
internal val DarkStudio = StudioColors(Color(0xFF211D24), Color(0xFF2C2730), Color(0xFFFFF7F2),
    Color(0xFFD0C5D2), Color(0xFF514750), Color(0xFF4B302A), Color(0xFF3C304F),
    Color(0xFF263F32), coral = Color(0xFFFF9A85), coralLight = Color(0xFFE77761),
    green = Color(0xFFA6E6BA), purple = Color(0xFFD3BDF5))
/** Approved third appearance. Light/Dark palettes remain unchanged. */
internal val NeonStudio = StudioColors(
    background = Color(0xFF071126),
    card = Color(0xFF13213C),
    ink = Color(0xFFF3F6FF),
    muted = Color(0xFFB2C1DE),
    border = Color(0xFF34466A),
    peach = Color(0xFF15365C),
    lavender = Color(0xFF1B2C54),
    mint = Color(0xFF103B48),
    coral = Color(0xFF35B7FF),
    coralLight = Color(0xFF5474FF),
    green = Color(0xFF82EBC4),
    purple = Color(0xFFB79CF5)
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
