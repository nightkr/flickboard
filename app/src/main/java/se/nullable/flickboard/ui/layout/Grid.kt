package se.nullable.flickboard.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Lays out a fixed grid of items. Compared to a row of columns, it enforces a uniform width between
 * all columns. Cells can span an integer number of columns using [GridRowScope.colspan].
 */
@Composable
fun Grid(
    modifier: Modifier = Modifier,
    columnGap: Dp = 0.dp,
    rowGap: Dp = 0.dp,
    rows: List<@Composable GridRowScope.() -> Unit>
) {
    Layout(rows.map { { GridRowScope().it() } }, modifier) { measurables, constraints ->
        fun Measurable.gridParentData(): GridParentData =
            (this.parentData as GridParentData?) ?: GridParentData.Default

        val columnGapPx = columnGap.roundToPx()
        val rowGapPx = rowGap.roundToPx()
        val columns = measurables.maxOf { row ->
            row.sumOf { it.gridParentData().colspan.toDouble() }.toFloat()
        }
        val totalFixedWidth = measurables.maxOf { rows ->
            rows.sumOf { (it.gridParentData().fixedWidth?.roundToPx() ?: 0) }
        }
        val wholeColumns = columns.roundToInt()
        val gaplessTotalWidth =
            (constraints.maxWidth - totalFixedWidth - (columnGapPx * (wholeColumns - 1)))
        val columnWidth = gaplessTotalWidth / columns
        var totalHeight = (measurables.size - 1) * rowGapPx
        val xPositions = mutableMapOf<Placeable, Int>()
        val placeableRows = measurables.map { measurableRow ->
            var x = 0F
            measurableRow.map { measurable ->
                val parentData = measurable.gridParentData()
                val width = when {
                    parentData.fixedWidth != null -> parentData.fixedWidth.roundToPx().toFloat()
                        .coerceAtLeast(0F)

                    else -> (columnWidth * parentData.colspan +
                            columnGapPx * (parentData.colspan - 1))
                        .coerceAtLeast(0F)
                }
                val columnConstraints =
                    constraints.copy(minWidth = width.toInt(), maxWidth = width.toInt())
                measurable.measure(columnConstraints).also {
                    xPositions[it] = x.toInt()
                    x += width + columnGapPx
                }
            }.also { placeableRow -> totalHeight += placeableRow.maxOf { it.height } }
        }

        layout(constraints.maxWidth, totalHeight) {
            var yPosition = 0
            placeableRows.forEach { placeableRow ->
                var rowHeight = 0
                placeableRow.forEach { placeable ->
                    placeable.place(x = xPositions[placeable]!!, y = yPosition)
                    rowHeight = max(rowHeight, placeable.height)
                }
                yPosition += rowHeight + rowGapPx
            }
        }
    }
}

class GridRowScope {
    fun Modifier.colspan(colspan: Float) = this then GridColspanElement(colspan = colspan)
    fun Modifier.fixedWidth(fixedWidth: Dp) =
        this then GridFixedWidthElement(fixedWidth = fixedWidth)
}

private data class GridParentData(val colspan: Float = 1F, val fixedWidth: Dp? = null) {
    companion object {
        val Default = GridParentData()
    }
}

private data class GridColspanElement(val colspan: Float) : ModifierNodeElement<GridColspanNode>() {
    override fun create(): GridColspanNode = GridColspanNode(colspan = colspan)
    override fun update(node: GridColspanNode) {
        node.colspan = colspan
    }
}

private class GridColspanNode(var colspan: Float) : ParentDataModifierNode, Modifier.Node() {
    override fun Density.modifyParentData(parentData: Any?): Any =
        ((parentData as GridParentData?) ?: GridParentData.Default).copy(colspan = colspan)
}

private data class GridFixedWidthElement(val fixedWidth: Dp) :
    ModifierNodeElement<GridFixedWidthNode>() {
    override fun create(): GridFixedWidthNode = GridFixedWidthNode(fixedWidth = fixedWidth)
    override fun update(node: GridFixedWidthNode) {
        node.fixedWidth = fixedWidth
    }
}

private class GridFixedWidthNode(var fixedWidth: Dp) : ParentDataModifierNode,
    Modifier.Node() {
    override fun Density.modifyParentData(parentData: Any?): Any =
        ((parentData as GridParentData?) ?: GridParentData.Default)
            .copy(fixedWidth = fixedWidth, colspan = 0F)
}
