package se.nullable.flickboard.ui

import android.gesture.GestureLibraries
import android.gesture.GestureOverlayView
import android.gesture.GestureOverlayView.OnGestureListener
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import se.nullable.flickboard.R
import se.nullable.flickboard.model.Gesture
import kotlin.math.min

// Only used as a preview
// Used to edit raw/gestures.txt
@Composable
@Preview
fun DollarOneGestureEditor() {
    val context = LocalContext.current
    val gestureLibrary = remember(context) {
        GestureLibraries.fromPrivateFile(context, "gestures.txt")
            .also { it.load() }
    }
    val refreshGestureLibrary = remember { mutableStateOf(Unit, neverEqualPolicy()) }
    val gesturePickerExpanded = remember { mutableStateOf(false) }
    val activeGestureName = remember {
        mutableStateOf(Gesture.names.keys.first())
    }
    Column {
        LazyColumn(Modifier.fillMaxHeight(0.5F)) {
            refreshGestureLibrary.value
            Gesture.names.keys.forEach { gestureName ->
                item(gestureName) {
                    Text(gestureName)
                }
                items(
                    gestureLibrary.getGestures(gestureName) ?: emptyList(),
                    key = { i -> "$gestureName.${i.hashCode()}" }) { gesture ->
                    Row {
                        Box(
                            Modifier
                                .size(100.dp)
                                .drawWithCache {
                                    val path = gesture
                                        .toPath()
                                        .asComposePath()
                                        .also { path ->
                                            val bounds = path.getBounds()
                                            path.translate(-bounds.topLeft)
                                            path.transform(Matrix().also {
                                                val scale = min(
                                                    this.size.width / bounds.width,
                                                    this.size.height / bounds.height,
                                                )
                                                it.scale(x = scale, y = scale)
                                            })
                                        }
                                    onDrawBehind {
                                        drawRect(Color.Black)
                                        drawPath(
                                            path,
                                            color = Color.Yellow,
                                            style = Stroke(3.dp.toPx())
                                        )
                                    }
                                })
                        Button(onClick = {
                            gestureLibrary.removeGesture(gestureName, gesture)
                            gestureLibrary.save()
                            refreshGestureLibrary.value = Unit
                        }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
        Column(Modifier.background(Color.Gray)) {
            Box {
                Button(onClick = { gesturePickerExpanded.value = true }) {
                    Text(activeGestureName.value)
                    Icon(
                        painterResource(R.drawable.baseline_arrow_drop_down_24),
                        contentDescription = "open dropdown"
                    )
                }
                DropdownMenu(
                    expanded = gesturePickerExpanded.value,
                    onDismissRequest = { gesturePickerExpanded.value = false }) {
                    Gesture.names.keys.forEach { gestureName ->
                        DropdownMenuItem(
                            text = { Text(gestureName) },
                            onClick = {
                                activeGestureName.value = gestureName
                                gesturePickerExpanded.value = false
                            })
                    }
                }
            }
            // Deduplicate gesture listener
            val isGesturing = remember { mutableStateOf(false) }
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                factory = ::GestureOverlayView,
                update = {
                    it.isEventsInterceptionEnabled = true
                    it.gestureStrokeAngleThreshold = 180F
                    it.gestureStrokeLengthThreshold = 0F
                    it.addOnGestureListener(object : OnGestureListener {
                        override fun onGestureStarted(
                            overlay: GestureOverlayView?,
                            event: MotionEvent?
                        ) {
                            isGesturing.value = true
                        }

                        override fun onGesture(overlay: GestureOverlayView?, event: MotionEvent?) {}

                        override fun onGestureEnded(
                            overlay: GestureOverlayView?,
                            event: MotionEvent?
                        ) {
                        }

                        override fun onGestureCancelled(
                            overlay: GestureOverlayView?,
                            event: MotionEvent?
                        ) {
                        }
                    })
                    it.addOnGesturePerformedListener { overlay, gesture ->
                        if (isGesturing.value) {
                            isGesturing.value = false
                            gestureLibrary.addGesture(activeGestureName.value, gesture)
                            gestureLibrary.save()
                            refreshGestureLibrary.value = Unit
                        }
                    }
                })
        }
    }
}
