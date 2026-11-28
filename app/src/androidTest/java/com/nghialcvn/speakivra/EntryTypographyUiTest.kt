package com.nghialcvn.speakivra

import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class EntryTypographyUiTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private fun shot(name: String) {
        val bitmap = ui.onRoot().captureToImage().asAndroidBitmap()
        val file = File(ui.activity.getExternalFilesDir(null), "ui-screenshots/$name.png")
        file.parentFile!!.mkdirs()
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    @Test fun approvedEntryHasStaticLogoMaskedMovingWordmarkAndThreeDimensionalTitles() {
        var phase by mutableStateOf(-.4f)
        var appearance by mutableStateOf(1)
        var selected: StudyLanguage? = null
        ui.runOnUiThread { ui.activity.setContent {
            SpeakivraTheme(dark = appearance == 1, neon = appearance == 2) {
                Surface(color = studio.background, contentColor = studio.ink) {
                    Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                        CompositionLocalProvider(LocalBrandSweepPhase provides phase) {
                            LanguageEntryScreen { selected = it }
                        }
                    }
                }
            }
        } }
        val word = ui.onNodeWithTag("entry-brand-wordmark")
        val logo = ui.onNodeWithTag("entry-brand-logo")
        val first = word.captureToImage().asAndroidBitmap()
        val symbol = logo.captureToImage().asAndroidBitmap()
        ui.runOnIdle { phase = .35f }
        val second = word.captureToImage().asAndroidBitmap()
        val symbolAfter = logo.captureToImage().asAndroidBitmap()
        assertFalse("The light must move", first.sameAs(second))
        assertTrue("The symbol must remain still", symbol.sameAs(symbolAfter))
        // A light sweep may alter letter pixels only. The approved transparent mask
        // is sampled with a safe margin to exclude resampling at letter edges.
        val mask = android.graphics.BitmapFactory.decodeResource(ui.activity.resources, R.drawable.brand_wordmark)
        val scale = minOf(first.width.toFloat() / mask.width, first.height.toFloat() / mask.height)
        val left = (first.width - mask.width * scale) / 2
        val top = (first.height - mask.height * scale) / 2
        var outside = 0
        for (y in 0 until first.height step 2) for (x in 0 until first.width step 2) {
            val sx = ((x - left) / scale).toInt()
            val sy = ((y - top) / scale).toInt()
            val clear = (-2..2).all { dy -> (-2..2).all { dx ->
                val px = sx + dx; val py = sy + dy
                px !in 0 until mask.width || py !in 0 until mask.height ||
                    android.graphics.Color.alpha(mask.getPixel(px, py)) == 0
            } }
            if (clear) { outside++; assertEquals(first.getPixel(x, y), second.getPixel(x, y)) }
        }
        assertTrue(outside > 100)
        listOf(first, second, symbol, symbolAfter, mask).forEach { it.recycle() }
        shot("180-entry-type-dark-sweep-left")
        ui.runOnIdle { phase = .75f }
        shot("181-entry-type-dark-sweep-right")
        for ((theme, name) in listOf(0 to "182-entry-type-light", 2 to "183-entry-type-neon")) {
            ui.runOnIdle { appearance = theme }
            shot(name)
        }
        ui.onNodeWithText("Tiếng Anh").assertIsDisplayed()
        ui.onNodeWithText("Tiếng Trung").assertIsDisplayed()
        ui.onNodeWithTag("language-en").performClick()
        ui.runOnIdle { assertEquals(StudyLanguage.ENGLISH, selected) }
        ui.onNodeWithTag("language-zh").performClick()
        ui.runOnIdle { assertEquals(StudyLanguage.CHINESE, selected) }
    }

    @Test fun titlesStayOnOneLineInsideTheirCardsAtNarrowWidthsAndLargeFont() {
        var width by mutableStateOf(360)
        var fontScale by mutableStateOf(1f)
        ui.runOnUiThread { ui.activity.setContent {
            SpeakivraTheme(true) {
                Surface(color = studio.background, contentColor = studio.ink) {
                    val density = LocalDensity.current.density
                    CompositionLocalProvider(LocalDensity provides Density(density, fontScale), LocalBrandSweepPhase provides .35f) {
                        Box(Modifier.width(width.dp).fillMaxHeight()) { LanguageEntryScreen {} }
                    }
                }
            }
        } }
        for (w in listOf(320, 360)) for (scale in listOf(1f, 1.5f)) {
            ui.runOnIdle { width = w; fontScale = scale }
            for ((label, card) in listOf("Tiếng Anh" to "language-en", "Tiếng Trung" to "language-zh")) {
                val title = ui.onNodeWithTag("entry-title-$label", useUnmergedTree = true)
                title.performScrollTo().assertIsDisplayed()
                val results = mutableListOf<TextLayoutResult>()
                title.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
                if (results.single().hasVisualOverflow) shot("185-overflow-${w}-$scale")
                assertEquals(1, results.single().lineCount)
                assertFalse("$label at ${w}dp / font $scale: ${results.single().size}, " +
                    "width overflow=${results.single().didOverflowWidth}, height overflow=${results.single().didOverflowHeight}",
                    results.single().hasVisualOverflow)
                val bounds = title.fetchSemanticsNode().boundsInRoot
                val parent = ui.onNodeWithTag(card).fetchSemanticsNode().boundsInRoot
                assertTrue(bounds.left >= parent.left && bounds.right + 2 <= parent.right)
            }
        }
        shot("184-entry-type-large-font")
    }
}
