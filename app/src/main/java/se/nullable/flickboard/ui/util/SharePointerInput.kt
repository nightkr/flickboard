package se.nullable.flickboard.ui.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.unit.IntSize

/**
 * Allows pointer events to pass through to the item behind it.
 */
fun Modifier.sharePointerInput(): Modifier = this then SharePointerInputElement

private data object SharePointerInputElement : ModifierNodeElement<SharePointerInputNode>() {
    override fun create(): SharePointerInputNode = SharePointerInputNode()

    override fun update(node: SharePointerInputNode) {}
}

private class SharePointerInputNode : PointerInputModifierNode, Modifier.Node() {
    override fun onCancelPointerInput() {}

    @Suppress("EmptyMethod")
    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize
    ) {
    }

    override fun sharePointerInputWithSiblings(): Boolean = true
}