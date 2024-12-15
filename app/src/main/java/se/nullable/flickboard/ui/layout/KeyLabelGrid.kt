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

/**
 * Tries to lay out a 3x3 grid of labels, such that:
 * - They have a uniform size
 * - The center item is scaled up somewhat (the "center bias")
 * - They do not overlay
 * - Corner labels duck out of the way of the corner radius
 */
@Composable
fun KeyLabelGrid(
    modifier: Modifier = Modifier,
    cornerRoundness: Float = 0F,
    content: @Composable KeyLabelGridScope.() -> Unit = {}
) {
    val centerBias = LocalAppSettings.current.actionVisualBiasCenter.state.value
    Layout(
        content = { KeyLabelGridScope().content() },
        modifier = modifier,
    ) { measurables, constraints ->
        val xCornerInset = sqrt(constraints.maxWidth * cornerRoundness).toInt() * 2
        val yCornerInset = sqrt(constraints.maxHeight * cornerRoundness).toInt() * 2
        val safeWidth = constraints.maxWidth - xCornerInset
        val safeHeight = constraints.maxHeight - yCornerInset
        val outerCellWidth = safeWidth / (2 + centerBias)
        val outerCellHeight = safeHeight / (2 + centerBias)

        val parentData =
            measurables.map {
                it.parentData as KeyLabelGridParentData? ?: KeyLabelGridParentData.Default
            }

        val placeables = measurables.zip(parentData) { measurable, itemParentData ->
            var itemConstraints = when (itemParentData.direction) {
                Direction.CENTER -> Constraints(
                    maxWidth = (outerCellWidth * centerBias).toInt(),
                    maxHeight = (outerCellHeight * centerBias).toInt(),
                )

                else -> Constraints(
                    maxWidth = outerCellWidth.toInt(),
                    maxHeight = outerCellHeight.toInt(),
                )
            }
            if (!itemParentData.restrictWidth) {
                // Hack to avoid wide text labels clipping...
                itemConstraints = itemConstraints.copy(maxWidth = safeWidth)
            }
            measurable.measure(itemConstraints)
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

class KeyLabelGridScope {
    fun Modifier.direction(direction: Direction) = this then KeyLabelGridDirectionElement(direction)
    fun Modifier.unrestrictedWidth() = this then KeyLabelGridUnrestrictedWidthElement
}

private data class KeyLabelGridParentData(
    val direction: Direction = Direction.CENTER,
    val restrictWidth: Boolean = true
) {
    companion object {
        val Default = KeyLabelGridParentData()
    }
}

private data class KeyLabelGridDirectionElement(val direction: Direction) :
    ModifierNodeElement<KeyLabelGridDirectionNode>() {
    override fun create(): KeyLabelGridDirectionNode =
        KeyLabelGridDirectionNode(direction = direction)

    override fun update(node: KeyLabelGridDirectionNode) {
        node.direction = direction
    }
}

private class KeyLabelGridDirectionNode(var direction: Direction) : ParentDataModifierNode,
    Modifier.Node() {
    override fun Density.modifyParentData(parentData: Any?): Any =
        ((parentData as KeyLabelGridParentData?)
            ?: KeyLabelGridParentData.Default).copy(direction = direction)
}

private data object KeyLabelGridUnrestrictedWidthElement :
    ModifierNodeElement<KeyLabelGridUnrestrictedWidthNode>() {
    override fun create(): KeyLabelGridUnrestrictedWidthNode = KeyLabelGridUnrestrictedWidthNode()

    override fun update(node: KeyLabelGridUnrestrictedWidthNode) {}
}

private class KeyLabelGridUnrestrictedWidthNode : ParentDataModifierNode, Modifier.Node() {
    override fun Density.modifyParentData(parentData: Any?): Any =
        ((parentData as KeyLabelGridParentData?)
            ?: KeyLabelGridParentData.Default).copy(restrictWidth = false)
}
