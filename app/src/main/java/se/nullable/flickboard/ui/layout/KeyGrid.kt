package se.nullable.flickboard.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.times
import se.nullable.flickboard.ui.LocalAppSettings
import kotlin.math.sqrt

@Composable
fun KeyGrid(
    modifier: Modifier = Modifier,
    cornerRoundness: Float = 0F,
    content: @Composable KeyGridScope.() -> Unit = {}
) {
    val centerBias = LocalAppSettings.current.actionVisualBiasCenter.state.value
    Layout(
        content = { KeyGridScope().content() },
        modifier = modifier
    ) { measurables, constraints ->
        val xCornerInset = sqrt(constraints.maxWidth * cornerRoundness).toInt() * 2
        val yCornerInset = sqrt(constraints.maxHeight * cornerRoundness).toInt() * 2
        val safeWidth = constraints.maxWidth - xCornerInset
        val safeHeight = constraints.maxHeight - yCornerInset
        val outerCellHeight = safeWidth / (2 + centerBias)
        val outerCellWidth = safeHeight / (2 + centerBias)

        val parentData =
            measurables.map { it.parentData as KeyGridParentData? ?: KeyGridParentData.Default }

        val placeables = measurables.zip(parentData) { measurable, itemParentData ->
            measurable.measure(
                when (itemParentData.direction) {
                    Direction.CENTER -> Constraints(
                        maxWidth = (outerCellWidth * centerBias).toInt(),
                        maxHeight = (outerCellHeight * centerBias).toInt(),
                    )

                    else -> Constraints(
                        maxWidth = outerCellWidth.toInt(),
                        maxHeight = outerCellHeight.toInt(),
                    )
                }
            )
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.zip(parentData) { placeable, itemParentData ->
                val direction = itemParentData.direction
                val xInset = xCornerInset * direction.isCorner()
                val yInset = yCornerInset * direction.isCorner()
                placeable.place(
                    x = when (direction) {
                        Direction.TOP_LEFT, Direction.LEFT, Direction.BOTTOM_LEFT -> xInset
                        Direction.TOP, Direction.CENTER, Direction.BOTTOM -> (constraints.maxWidth - placeable.width) / 2
                        Direction.TOP_RIGHT, Direction.RIGHT, Direction.BOTTOM_RIGHT -> constraints.maxWidth - placeable.width - xInset
                    },
                    y = when (direction) {
                        Direction.TOP_LEFT, Direction.TOP, Direction.TOP_RIGHT -> yInset
                        Direction.LEFT, Direction.CENTER, Direction.RIGHT -> (constraints.maxHeight - placeable.height) / 2
                        Direction.BOTTOM_LEFT, Direction.BOTTOM, Direction.BOTTOM_RIGHT -> constraints.maxHeight - placeable.height - yInset
                    },
                )
            }
        }
    }
}

class KeyGridScope {
    fun Modifier.direction(direction: Direction) = this then KeyGridDirectionElement(direction)
}

private data class KeyGridParentData(val direction: Direction = Direction.CENTER) {
    companion object {
        val Default = KeyGridParentData()
    }
}

private data class KeyGridDirectionElement(val direction: Direction) :
    ModifierNodeElement<KeyGridDirectionNode>() {
    override fun create(): KeyGridDirectionNode = KeyGridDirectionNode(direction = direction)
    override fun update(node: KeyGridDirectionNode) {
        node.direction = direction
    }
}

private class KeyGridDirectionNode(var direction: Direction) : ParentDataModifierNode,
    Modifier.Node() {
    override fun Density.modifyParentData(parentData: Any?): Any =
        ((parentData as KeyGridParentData?)
            ?: KeyGridParentData.Default).copy(direction = direction)
}
