package se.nullable.flickboard.model.layouts

import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.SearchDirection

// This should only be a side-layer and not a "proper" numbers layer, since it doesn't fill the
// regular 3x3+2 layout and can't be safely merged into a letters layer.

val MINI_NUMBERS_SYMBOLS_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.TOP_LEFT to Action.Text("+"),
                    Direction.TOP to Action.Text("*"),
                    Direction.TOP_RIGHT to Action.Text("="),
                    Direction.BOTTOM_LEFT to Action.Text("-"),
                    Direction.BOTTOM to Action.Text("^"),
                    Direction.BOTTOM_RIGHT to Action.Text("_"),
                ),
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.TOP_LEFT to Action.Text("("),
                    Direction.TOP to Action.JumpLineKeepPos(SearchDirection.Backwards),
                    Direction.TOP_RIGHT to Action.Text(")"),
                    Direction.BOTTOM_LEFT to Action.Text("#"),
                    Direction.BOTTOM to Action.JumpLineKeepPos(SearchDirection.Forwards),
                    Direction.BOTTOM_RIGHT to Action.Text("$"),
                ),
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.TOP_LEFT to Action.Text("&"),
                    Direction.TOP to Action.Text("@"),
                    Direction.TOP_RIGHT to Action.Text("~"),
                    Direction.BOTTOM_LEFT to Action.Text("!"),
                    Direction.BOTTOM to Action.Text("|"),
                    Direction.BOTTOM_RIGHT to Action.Text("?"),
                ),
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.TOP_LEFT to Action.Text("\""),
                    Direction.TOP to Action.Text("'"),
                    Direction.TOP_RIGHT to Action.Text(";"),
                    Direction.LEFT to Action.Jump(SearchDirection.Backwards),
                    Direction.RIGHT to Action.Jump(SearchDirection.Forwards),
                    Direction.BOTTOM_LEFT to Action.Text(","),
                    Direction.BOTTOM to Action.Text("."),
                    Direction.BOTTOM_RIGHT to Action.Text(":"),
                ),
            ),
        ),
    ),
)

fun miniNumbersPhoneLayer(digits: String) = Layer(
    keyRows = listOf(
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.LEFT to Action.Text(digits[1].toString()),
                    Direction.CENTER to Action.Text(digits[2].toString()),
                    Direction.RIGHT to Action.Text(digits[3].toString()),
                ),
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.LEFT to Action.Text(digits[4].toString()),
                    Direction.CENTER to Action.Text(digits[5].toString()),
                    Direction.RIGHT to Action.Text(digits[6].toString()),
                ),
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.LEFT to Action.Text(digits[7].toString()),
                    Direction.CENTER to Action.Text(digits[8].toString()),
                    Direction.RIGHT to Action.Text(digits[9].toString()),
                ),
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(Direction.CENTER to Action.Text(digits[0].toString())),
            ),
        ),
    ),
)

fun miniNumbersCalculatorLayer(digits: String) =
    miniNumbersPhoneLayer(reorderDigitsForCalculatorLayout(digits))
