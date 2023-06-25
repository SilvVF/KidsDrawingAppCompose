package my.packlol.kidsdrawingapp.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import my.packlol.kidsdrawingapp.data.DragData
import my.packlol.kidsdrawingapp.drawing.DrawingAction
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Suppress("BanInlineOptIn")
@OptIn(ExperimentalContracts::class, ExperimentalContracts::class)
inline fun <T> List<T>.fastForEach(action: (T) -> Unit) {
    contract { callsInPlace(action) }
    for (index in indices) {
        val item = get(index)
        action(item)
    }
}

@Composable
fun DrawingCanvas(
    modifier: Modifier,
    dragData: List<DragData>,
    actionSink: (DrawingAction) -> Unit,
    onTap: (Offset) -> Unit,
    onDragStarted: (Offset) -> Unit,
    onDraw: (start: Offset, end: Offset) -> Unit
) {

    var prevEnd by remember {
        mutableStateOf(Offset.Zero)
    }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onTap(it)
                        actionSink(DrawingAction.DragEnded)
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onDragStarted(offset)
                        prevEnd = offset
                    },
                    onDrag = { change: PointerInputChange, _: Offset ->
                        onDraw(prevEnd, change.position,)
                        prevEnd = change.position
                    },
                    onDragEnd = {
                        actionSink(DrawingAction.DragEnded)
                        prevEnd = Offset.Zero
                    }
                )
            },
    ) {
        dragData.fastForEach { data ->
            if (data.circle) {
                drawCircle(
                    radius = data.width,
                    center = data.start,
                    color = data.color
                )
            } else {
                drawPath(
                    path = data.path,
                    color = data.color,
                    style = Stroke(
                        width = data.width,
                        cap = StrokeCap.Round
                    )
                )
            }
        }
    }
}