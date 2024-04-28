package se.nullable.flickboard.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import se.nullable.flickboard.ui.Labeled
import se.nullable.flickboard.util.hct.Hct
import se.nullable.flickboard.util.hct.HctSolver

private fun Color.toHct(): Hct = Hct.fromInt(toArgb())
fun colourOfHctHue(hue: Int): Color = Color(HctSolver.solveToInt(hue.toDouble(), 100.0, 50.0))
private fun Hct.toColour(): Color = Color(toInt())

enum class MaterialToneMode(override val label: String) : Labeled {
    System("System"),
    Light("Light"),
    Dark("Dark"),
    Midnight("Midnight");

    val config: MaterialToneConfig
        @Composable
        get() = when (this) {
            System -> when {
                isSystemInDarkTheme() -> MaterialToneConfig.dark
                else -> MaterialToneConfig.light
            }

            Light -> MaterialToneConfig.light
            Dark -> MaterialToneConfig.dark
            Midnight -> MaterialToneConfig.midnight
        }
}

data class MaterialToneConfig(
    val accent: Int,
    val onAccent: Int,
    val accentContainer: Int,
    val onAccentContainer: Int
) {
    companion object {
        // Constants based on Google/Material Design 3
        val light = MaterialToneConfig(
            accent = 40, onAccent = 100, accentContainer = 90, onAccentContainer = 10
        )
        val dark = MaterialToneConfig(
            accent = 80, onAccent = 20, accentContainer = 30, onAccentContainer = 90
        )
        val midnight = MaterialToneConfig(
            accent = 40, onAccent = 0, accentContainer = 0, onAccentContainer = 60
        )
    }
}

private fun Color.hctRotateHue(hueOffset: Int): Color =
    toHct().also { it.hue += hueOffset }.toColour()

fun Color.toTertiary() = hctRotateHue(240)

private fun Color.hctSetCt(chroma: Float, tone: Int): Color =
    toHct().also {
        it.chroma = chroma.toDouble()
        it.tone = tone.toDouble()
    }.toColour()

fun Color.toAccent(chroma: Float, toneConfig: MaterialToneConfig) =
    hctSetCt(chroma = chroma, toneConfig.accent)

fun Color.toOnAccent(chroma: Float, toneConfig: MaterialToneConfig) =
    hctSetCt(chroma = chroma, toneConfig.onAccent)

fun Color.toAccentContainer(chroma: Float, toneConfig: MaterialToneConfig) =
    hctSetCt(chroma = chroma, toneConfig.accentContainer)

fun Color.toOnAccentContainer(chroma: Float, toneConfig: MaterialToneConfig) =
    hctSetCt(chroma = chroma, toneConfig.onAccentContainer)
