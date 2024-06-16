package se.nullable.flickboard.model.layouts

import org.junit.Assert
import org.junit.Test
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.ui.LetterLayerOption

class SymbolLayerTest {
    @Test
    fun shouldNotBeShadowedByLetters() {
        fun assertKeyNoOverlap(letterLayer: LetterLayerOption, symbolKey: KeyM, letterKey: KeyM) {
            symbolKey.actions.entries.forEach { (direction, symbolAction) ->
                val letterAction = letterKey.actions[direction]
                when (Pair(letterLayer, letterAction)) {
                    // exclude known conflicts
                    Pair(LetterLayerOption.French, Action.Text("â")) -> {}
                    Pair(LetterLayerOption.French, Action.Text("Â")) -> {}
                    Pair(LetterLayerOption.French, Action.Text("Ç")) -> {}
                    Pair(LetterLayerOption.French, Action.Text("ê")) -> {}
                    Pair(LetterLayerOption.French, Action.Text("è")) -> {}
                    Pair(LetterLayerOption.French, Action.Text("Ê")) -> {}
                    Pair(LetterLayerOption.French, Action.Text("È")) -> {}
                    Pair(LetterLayerOption.FrenchExt, Action.Text("W")) -> {}
                    Pair(LetterLayerOption.FrenchExt, Action.Text("Ë")) -> {}
                    Pair(LetterLayerOption.FrenchExt, Action.Text("Ü")) -> {}
                    Pair(LetterLayerOption.FrenchPunc, Action.Text("Ç")) -> {}
                    Pair(LetterLayerOption.FrenchPunc, Action.Text("è")) -> {}
                    Pair(LetterLayerOption.FrenchPunc, Action.Text("È")) -> {}
                    Pair(LetterLayerOption.German, Action.Text("ä")) -> {}
                    Pair(LetterLayerOption.German, Action.Text("Ä")) -> {}
                    Pair(LetterLayerOption.German, Action.Text("Ö")) -> {}
                    Pair(LetterLayerOption.GermanEnglish, Action.Text("ü")) -> {}
                    Pair(LetterLayerOption.GermanEnglish, Action.Text("Ü")) -> {}
                    Pair(LetterLayerOption.GermanEsperanto, Action.Text("ä")) -> {}
                    Pair(LetterLayerOption.GermanEsperanto, Action.Text("Ä")) -> {}
                    Pair(LetterLayerOption.GermanEsperanto, Action.Text("Ö")) -> {}
                    Pair(LetterLayerOption.GermanEsperanto, Action.Text("Ch")) -> {}
                    Pair(LetterLayerOption.GermanEsperanto, Action.Text("Sch")) -> {}
                    Pair(LetterLayerOption.Hungarian, Action.Text("ú")) -> {}
                    Pair(LetterLayerOption.Hungarian, Action.Text("Ú")) -> {}
                    Pair(LetterLayerOption.Hungarian, Action.Text("Ű")) -> {}
                    Pair(LetterLayerOption.Hungarian, Action.Text("ö")) -> {}
                    Pair(LetterLayerOption.Hungarian, Action.Text("Ö")) -> {}
                    Pair(LetterLayerOption.HungarianDT, Action.Text("á")) -> {}
                    Pair(LetterLayerOption.HungarianDT, Action.Text("é")) -> {}
                    Pair(LetterLayerOption.HungarianDT, Action.Text("ú")) -> {}
                    Pair(LetterLayerOption.HungarianDT, Action.Text("ó")) -> {}
                    Pair(LetterLayerOption.HungarianDT, Action.Text("Á")) -> {}
                    Pair(LetterLayerOption.HungarianDT, Action.Text("É")) -> {}
                    Pair(LetterLayerOption.HungarianDT, Action.Text("Ú")) -> {}
                    Pair(LetterLayerOption.HungarianDT, Action.Text("Ó")) -> {}
                    Pair(LetterLayerOption.HungarianDT, Action.Text("í")) -> {}
                    Pair(LetterLayerOption.HungarianDT, Action.Text("Í")) -> {}
                    Pair(LetterLayerOption.HungarianDT, Action.Text("ü")) -> {}
                    Pair(LetterLayerOption.HungarianDT, Action.Text("ű")) -> {}
                    Pair(LetterLayerOption.HungarianDT, Action.Text("Ü")) -> {}
                    Pair(LetterLayerOption.HungarianDT, Action.Text("Ű")) -> {}
                    Pair(LetterLayerOption.HungarianDT, Action.Text("ö")) -> {}
                    Pair(LetterLayerOption.HungarianDT, Action.Text("ő")) -> {}
                    Pair(LetterLayerOption.HungarianDT, Action.Text("Ö")) -> {}
                    Pair(LetterLayerOption.HungarianDT, Action.Text("Ő")) -> {}
                    Pair(LetterLayerOption.HungarianMF, Action.Text("á")) -> {}
                    Pair(LetterLayerOption.HungarianMF, Action.Text("Á")) -> {}
                    Pair(LetterLayerOption.HungarianMF, Action.Text("í")) -> {}
                    Pair(LetterLayerOption.HungarianMF, Action.Text("Í")) -> {}
                    Pair(LetterLayerOption.HungarianMF, Action.Text("ö")) -> {}
                    Pair(LetterLayerOption.HungarianMF, Action.Text("ő")) -> {}
                    Pair(LetterLayerOption.HungarianMF, Action.Text("Ö")) -> {}
                    Pair(LetterLayerOption.HungarianMF, Action.Text("Ő")) -> {}
                    Pair(LetterLayerOption.HungarianMF, Action.Text("Ó")) -> {}
                    Pair(LetterLayerOption.HungarianMF, Action.Text("ü")) -> {}
                    Pair(LetterLayerOption.HungarianMF, Action.Text("ű")) -> {}
                    Pair(LetterLayerOption.HungarianMF, Action.Text("Ü")) -> {}
                    Pair(LetterLayerOption.HungarianMF, Action.Text("Ű")) -> {}
                    Pair(LetterLayerOption.HungarianUUp, Action.Text("ó")) -> {}
                    Pair(LetterLayerOption.HungarianUUp, Action.Text("Ó")) -> {}
                    Pair(LetterLayerOption.HungarianUUp, Action.Text("Ö")) -> {}
                    Pair(LetterLayerOption.HungarianUUp, Action.Text("ű")) -> {}
                    Pair(LetterLayerOption.HungarianUUp, Action.Text("Ű")) -> {}
                    Pair(LetterLayerOption.Italian, Action.Text("Ò")) -> {}
                    Pair(LetterLayerOption.Italian, Action.Text("ó")) -> {}
                    Pair(LetterLayerOption.Italian, Action.Text("è")) -> {}
                    Pair(LetterLayerOption.Italian, Action.Text("Ó")) -> {}
                    Pair(LetterLayerOption.Italian, Action.Text("È")) -> {}
                    Pair(LetterLayerOption.PolishRmitura, Action.Text("ą")) -> {}
                    Pair(LetterLayerOption.PolishRmitura, Action.Text("Ą")) -> {}
                    Pair(LetterLayerOption.PolishRmitura, Action.Text("v")) -> {}
                    Pair(LetterLayerOption.PolishRmitura, Action.Text("x")) -> {}
                    Pair(LetterLayerOption.PolishRmitura, Action.Text("V")) -> {}
                    Pair(LetterLayerOption.PolishRmitura, Action.Text("X")) -> {}
                    Pair(LetterLayerOption.PolishRmitura, Action.Text("l")) -> {}
                    Pair(LetterLayerOption.PolishRmitura, Action.Text("L")) -> {}
                    Pair(LetterLayerOption.PolishRmitura, Action.Text("ć")) -> {}
                    Pair(LetterLayerOption.PolishRmitura, Action.Text("Ć")) -> {}
                    Pair(LetterLayerOption.PolishRmitura, Action.Text("ż")) -> {}
                    Pair(LetterLayerOption.PolishRmitura, Action.Text("Ż")) -> {}
                    Pair(LetterLayerOption.PolishRmitura, Action.Text("ś")) -> {}
                    Pair(LetterLayerOption.PolishRmitura, Action.Text("Ś")) -> {}
                    Pair(LetterLayerOption.Portuguese, Action.Text("á")) -> {}
                    Pair(LetterLayerOption.Portuguese, Action.Text("Á")) -> {}
                    Pair(LetterLayerOption.Portuguese, Action.Text("ñ")) -> {}
                    Pair(LetterLayerOption.Portuguese, Action.Text("Ñ")) -> {}
                    Pair(LetterLayerOption.Portuguese, Action.Text("ú")) -> {}
                    Pair(LetterLayerOption.Portuguese, Action.Text("ê")) -> {}
                    Pair(LetterLayerOption.Portuguese, Action.Text("Ú")) -> {}
                    Pair(LetterLayerOption.Portuguese, Action.Text("Ê")) -> {}
                    Pair(LetterLayerOption.PortugueseIos, Action.Text("à")) -> {}
                    Pair(LetterLayerOption.PortugueseIos, Action.Text("À")) -> {}
                    Pair(LetterLayerOption.PortugueseIos, Action.Text("ñ")) -> {}
                    Pair(LetterLayerOption.PortugueseIos, Action.Text("Ñ")) -> {}
                    Pair(LetterLayerOption.PortugueseIos, Action.Text("Ç")) -> {}
                    Pair(LetterLayerOption.PortugueseIos, Action.Text("ú")) -> {}
                    Pair(LetterLayerOption.PortugueseIos, Action.Text("ê")) -> {}
                    Pair(LetterLayerOption.PortugueseIos, Action.Text("Ú")) -> {}
                    Pair(LetterLayerOption.PortugueseIos, Action.Text("Ê")) -> {}
                    Pair(LetterLayerOption.Russian, Action.Text("ц")) -> {}
                    Pair(LetterLayerOption.Russian, Action.Text("Ц")) -> {}
                    Pair(LetterLayerOption.Russian, Action.Text("й")) -> {}
                    Pair(LetterLayerOption.Russian, Action.Text("Й")) -> {}
                    Pair(LetterLayerOption.Russian, Action.Text("Ъ")) -> {}
                    Pair(LetterLayerOption.Russian, Action.Text("ё")) -> {}
                    Pair(LetterLayerOption.Russian, Action.Text("э")) -> {}
                    Pair(LetterLayerOption.Russian, Action.Text("Ё")) -> {}
                    Pair(LetterLayerOption.Russian, Action.Text("Э")) -> {}
                    Pair(LetterLayerOption.RussianPhonetic, Action.Text("ч")) -> {}
                    Pair(LetterLayerOption.RussianPhonetic, Action.Text("Ч")) -> {}
                    Pair(LetterLayerOption.RussianPhonetic, Action.Text("ы")) -> {}
                    Pair(LetterLayerOption.RussianPhonetic, Action.Text("Ы")) -> {}
                    Pair(LetterLayerOption.RussianPhonetic, Action.Text("Ь")) -> {}
                    Pair(LetterLayerOption.RussianPhonetic, Action.Text("ё")) -> {}
                    Pair(LetterLayerOption.RussianPhonetic, Action.Text("щ")) -> {}
                    Pair(LetterLayerOption.RussianPhonetic, Action.Text("Ё")) -> {}
                    Pair(LetterLayerOption.RussianPhonetic, Action.Text("Щ")) -> {}
                    Pair(LetterLayerOption.Spanish, Action.Text("á")) -> {}
                    Pair(LetterLayerOption.Spanish, Action.Text("Á")) -> {}
                    Pair(LetterLayerOption.Spanish, Action.Text("ñ")) -> {}
                    Pair(LetterLayerOption.Spanish, Action.Text("Ñ")) -> {}
                    Pair(LetterLayerOption.Spanish, Action.Text("ú")) -> {}
                    Pair(LetterLayerOption.Spanish, Action.Text("Ú")) -> {}
                    Pair(LetterLayerOption.SwedishDE, Action.Text("Ä")) -> {}
                    Pair(LetterLayerOption.SwedishDE, Action.Text("å")) -> {}
                    Pair(LetterLayerOption.SwedishDE, Action.Text("Å")) -> {}
                    Pair(LetterLayerOption.Turkish, Action.Text("ç")) -> {}
                    Pair(LetterLayerOption.Turkish, Action.Text("Ç")) -> {}
                    Pair(LetterLayerOption.Turkish, Action.Text("Ğ")) -> {}
                    Pair(LetterLayerOption.Turkish, Action.Text("ü")) -> {}
                    Pair(LetterLayerOption.Turkish, Action.Text("Ü")) -> {}
                    Pair(LetterLayerOption.Turkish, Action.Text("ş")) -> {}
                    Pair(LetterLayerOption.Turkish, Action.Text("Ş")) -> {}
                    Pair(LetterLayerOption.Ukrainian, Action.Text("ц")) -> {}
                    Pair(LetterLayerOption.Ukrainian, Action.Text("Ц")) -> {}
                    Pair(LetterLayerOption.Ukrainian, Action.Text("й")) -> {}
                    Pair(LetterLayerOption.Ukrainian, Action.Text("Й")) -> {}
                    Pair(LetterLayerOption.Ukrainian, Action.Text("Ґ")) -> {}
                    Pair(LetterLayerOption.Ukrainian, Action.Text("ї")) -> {}
                    Pair(LetterLayerOption.Ukrainian, Action.Text("є")) -> {}
                    Pair(LetterLayerOption.Ukrainian, Action.Text("Ї")) -> {}
                    Pair(LetterLayerOption.Ukrainian, Action.Text("Є")) -> {}
                    Pair(LetterLayerOption.UkrainianRussian, Action.Text("ц")) -> {}
                    Pair(LetterLayerOption.UkrainianRussian, Action.Text("Ц")) -> {}
                    Pair(LetterLayerOption.UkrainianRussian, Action.Text("й")) -> {}
                    Pair(LetterLayerOption.UkrainianRussian, Action.Text("Й")) -> {}
                    Pair(LetterLayerOption.UkrainianRussian, Action.Text("э")) -> {}
                    Pair(LetterLayerOption.UkrainianRussian, Action.Text("Э")) -> {}
                    Pair(LetterLayerOption.UkrainianRussian, Action.Text("І")) -> {}
                    Pair(LetterLayerOption.UkrainianRussian, Action.Text("ї")) -> {}
                    Pair(LetterLayerOption.UkrainianRussian, Action.Text("є")) -> {}
                    Pair(LetterLayerOption.UkrainianRussian, Action.Text("Ї")) -> {}
                    Pair(LetterLayerOption.UkrainianRussian, Action.Text("Є")) -> {}
                    Pair(LetterLayerOption.UkrainianRussian, Action.Text("ё")) -> {}
                    Pair(LetterLayerOption.UkrainianRussian, Action.Text("Ё")) -> {}

                    else -> {
                        Assert.assertNull(
                            "Symbol $symbolAction shadowed by letter $letterAction in $letterLayer",
                            letterAction
                        )
                    }
                }
            }
            if (symbolKey.shift != null) {
                assertKeyNoOverlap(letterLayer, symbolKey.autoShift(), letterKey.autoShift())
            }
        }

        LetterLayerOption.entries.forEach { letterLayer ->
            MESSAGEASE_SYMBOLS_LAYER.keyRows.zip(letterLayer.layout.mainLayer.keyRows) { symbolRow, letterRow ->
                symbolRow.zip(letterRow) { symbolKey, letterKey ->
                    assertKeyNoOverlap(letterLayer, symbolKey, letterKey)
                }
            }
        }
    }
}