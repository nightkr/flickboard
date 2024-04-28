package se.nullable.flickboard.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import se.nullable.flickboard.util.hct.Hct
import se.nullable.flickboard.util.hct.HctSolver

private const val customColourChroma: Int = 20

fun Color.toHct(): Hct = Hct.fromInt(toArgb())
fun colourOfHctHue(hue: Int): Color = Color(HctSolver.solveToInt(hue.toDouble(), 60.0, 50.0))
fun Hct.toColour(): Color = Color(toInt())

data class MaterialToneConfig(
    val accent: Int,
    val onAccent: Int,
    val accentContainer: Int,
    val onAccentContainer: Int
) {
    companion object {
        // Constants from Google/Material Design 3
        private val light = MaterialToneConfig(
            accent = 40, onAccent = 100, accentContainer = 90, onAccentContainer = 10
        )
        private val dark = MaterialToneConfig(
            accent = 80, onAccent = 20, accentContainer = 30, onAccentContainer = 90
        )

        val current: MaterialToneConfig
            @Composable
            get() = when {
                isSystemInDarkTheme() -> dark
                else -> light
            }
    }
}

private fun Color.hctSetCt(chroma: Int, tone: Int): Color =
    toHct().also {
//        it.chroma = chroma.toDouble()
        it.tone = tone.toDouble()
    }.toColour()

fun Color.hctSetHue(hue: Double): Color =
    toHct().also { it.hue = hue }.toColour()

fun Color.hctSetChroma(chroma: Double): Color =
    toHct().also { it.chroma = chroma }.toColour()

fun Color.toAccent(toneConfig: MaterialToneConfig) =
    hctSetCt(chroma = customColourChroma, toneConfig.accent)

fun Color.toOnAccent(toneConfig: MaterialToneConfig) =
    hctSetCt(chroma = customColourChroma, toneConfig.onAccent)

fun Color.toAccentContainer(toneConfig: MaterialToneConfig) =
    hctSetCt(chroma = customColourChroma, toneConfig.accentContainer)

fun Color.toOnAccentContainer(toneConfig: MaterialToneConfig) =
    hctSetCt(chroma = customColourChroma, toneConfig.onAccentContainer)
