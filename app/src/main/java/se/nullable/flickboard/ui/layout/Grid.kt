package se.nullable.flickboard.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

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
        val columns = measurables.maxOf { row -> row.sumOf { it.gridParentData().colspan } }
        val gaplessTotalWidth = constraints.maxWidth - (columnGapPx * (columns - 1))
        val baseColumnWidth = gaplessTotalWidth / columns
        // The total column width may not be evenly divisble over all columns.
        // If so, we give a pixel each per column until we run out.
        // This should be less noticeable than enlarging the gaps since the columns will typically
        // be a lot larger than the gaps to begin with.
        val columnWidthRemainder = gaplessTotalWidth % columns
        var totalHeight = (measurables.size - 1) * rowGapPx
        val placeableRows = measurables.map { measurableRow ->
            var measuredColumns = 0
            measurableRow.map { measurable ->
                val parentData = measurable.gridParentData()
                val width = baseColumnWidth * parentData.colspan +
                        (columnWidthRemainder - measuredColumns)
                            .coerceIn(0..parentData.colspan) +
                        columnGapPx * (parentData.colspan - 1)
                val columnConstraints =
                    constraints.copy(
                        minWidth = width,
                        maxWidth = width,
                    )
                measuredColumns += parentData.colspan
                measurable.measure(columnConstraints)
            }.also { placeableRow -> totalHeight += placeableRow.maxOf { it.height } }
        }

        layout(constraints.maxWidth, totalHeight) {
            var yPosition = 0
            placeableRows.forEach { placeableRow ->
                var xPosition = 0
                var rowHeight = 0
                placeableRow.forEach { placeable ->
                    placeable.place(x = xPosition, y = yPosition)
                    xPosition += placeable.width + columnGapPx
                    rowHeight = max(rowHeight, placeable.height)
                }
                yPosition += rowHeight + rowGapPx
            }
        }
    }
}

class GridRowScope {
    fun Modifier.colspan(colspan: Int) = this then GridColspanElement(colspan = colspan)
}

private data class GridParentData(val colspan: Int = 1) {
    companion object {
        val Default = GridParentData()
    }
}

private data class GridColspanElement(val colspan: Int) : ModifierNodeElement<GridColspanNode>() {
    override fun create(): GridColspanNode = GridColspanNode(colspan = colspan)
    override fun update(node: GridColspanNode) {
        node.colspan = colspan
    }
}

private class GridColspanNode(var colspan: Int) : ParentDataModifierNode, Modifier.Node() {
    override fun Density.modifyParentData(parentData: Any?): Any =
        ((parentData as GridParentData?) ?: GridParentData.Default).copy(colspan = colspan)
}