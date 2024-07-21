package se.nullable.flickboard.model.layouts

import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.SearchDirection

// This should only be a side-layer and not a "proper" numbers layer, since it doesn't fill the
// regular 3x3+2 layout and can't be safely merged into a letters layer.

val FA_MINI_NUMBERS_SYMBOLS_LAYER = Layer(
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
                    Direction.TOP_RIGHT to Action.Text("؛"),
                    Direction.LEFT to Action.Jump(SearchDirection.Backwards),
                    Direction.RIGHT to Action.Jump(SearchDirection.Forwards),
                    Direction.BOTTOM_LEFT to Action.Text("،"),
                    Direction.BOTTOM to Action.Text("."),
                    Direction.BOTTOM_RIGHT to Action.Text(":"),
                ),
            ),
        ),
    )
)

val FA_MINI_NUMBERS_PHONE_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.LEFT to Action.Text("۱"),
                    Direction.CENTER to Action.Text("۲"),
                    Direction.RIGHT to Action.Text("۳"),
                ),
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.LEFT to Action.Text("۴"),
                    Direction.CENTER to Action.Text("۵"),
                    Direction.RIGHT to Action.Text("۶"),
                ),
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.LEFT to Action.Text("۷"),
                    Direction.CENTER to Action.Text("۸"),
                    Direction.RIGHT to Action.Text("۹"),
                ),
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(Direction.CENTER to Action.Text("۰")),
            ),
        ),
    ),
)

val FA_MINI_NUMBERS_CALCULATOR_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.LEFT to Action.Text("۷"),
                    Direction.CENTER to Action.Text("۸"),
                    Direction.RIGHT to Action.Text("۹"),
                ),
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.LEFT to Action.Text("۴"),
                    Direction.CENTER to Action.Text("۵"),
                    Direction.RIGHT to Action.Text("۶"),
                ),
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.LEFT to Action.Text("۱"),
                    Direction.CENTER to Action.Text("۲"),
                    Direction.RIGHT to Action.Text("۳"),
                ),
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(Direction.CENTER to Action.Text("۰")),
            ),
        ),
    ),
)
