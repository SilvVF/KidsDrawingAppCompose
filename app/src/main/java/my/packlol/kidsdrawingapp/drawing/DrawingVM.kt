package my.packlol.kidsdrawingapp.drawing

import android.view.View
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import my.packlol.kidsdrawingapp.data.DragData
import my.packlol.kidsdrawingapp.data.ImageSaver
import my.packlol.kidsdrawingapp.ui.EventViewModel
import java.util.Stack


class DrawingVM(
    private val imageSaver: ImageSaver,
): EventViewModel<DrawingEvent>() {

    private val deletedLine = Stack<DragData>()

    private val mutableDragData = MutableStateFlow<List<DragData>>(emptyList())

    val dragData = mutableDragData
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    fun handleAction(action: DrawingAction) = viewModelScope.launch {
        when(action) {
            is DrawingAction.SaveImage -> saveImage(action.view, action.clipHeight)
            is DrawingAction.OnDrag -> updateDragData(
                start = action.start,
                end = action.end,
                color = action.color,
                width = action.width
            )
            DrawingAction.Redo -> redo()
            DrawingAction.Undo -> undo()
            DrawingAction.DragEnded -> Unit
            is DrawingAction.DragStarted -> dragStarted(
                start = action.start,
                color = action.color,
                width = action.width
            )
            is DrawingAction.OnTap -> onTap(
                location = action.offset,
                color = action.color,
                width = action.width
            )
            DrawingAction.ClearDrawing -> {
                mutableDragData.emit(emptyList())
            }
        }
    }

    private fun undo() = viewModelScope.launch {
        mutableDragData.getAndUpdate { data ->
            if (data.isNotEmpty()) {
                deletedLine.push(data.last())
                data.dropLast(1)
            } else {
                mutableEvents.send(
                    DrawingEvent.Error("no lines to remove")
                )
                data
            }
        }
    }

    private fun redo() = viewModelScope.launch {
        deletedLine.takeIf { it.isNotEmpty() }
            ?.pop()
            ?.let { data ->
                mutableDragData.getAndUpdate {
                    it + data
                }
            }
            ?: mutableEvents.send(
                DrawingEvent.Error("no lines to replace")
            )
    }

    private fun onTap(
        location: Offset,
        color: Color,
        width: Float
    ) = viewModelScope.launch {
        mutableDragData.emit(
            buildList {
                addAll(mutableDragData.value)
                add(
                    DragData(
                        circle = true,
                        start = location,
                        path = Path(),
                        color = color,
                        width = width
                    )
                )
            }
        )
    }

    private fun dragStarted(
        start: Offset,
        color: Color,
        width: Float
    ) = viewModelScope.launch {
        mutableDragData.emit(
            buildList {
                addAll(mutableDragData.value)
                add(
                    DragData(
                        Path().apply {
                            moveTo(start.x, start.y)
                        },
                        color, width, false, start
                    )
                )
            }
        )
    }

    private fun saveImage(
        view: View,
        height: Int
    ) = viewModelScope.launch {
        mutableEvents.send(
            DrawingEvent.Message("Starting download")
        )
        runCatching {
            imageSaver.saveImage(
                view = view,
                height = height
            )
        }
            .onSuccess {
                mutableEvents.send(
                    DrawingEvent.Message("Finished dowloading")
                )
            }
            .onFailure {
                mutableEvents.send(
                    DrawingEvent.Error("Failed to download ${it.localizedMessage}")
                )
            }
    }

    private suspend fun updateDragData(
        start: Offset,
        end: Offset,
        color: Color,
        width: Float
    )  {
        val data = mutableDragData.value
        mutableDragData.emit(
            buildList {
                if (data.size > 1) {
                    addAll(data.subList(0, data.lastIndex))
                }
                val last = data.lastOrNull()
                    ?: DragData(Path(), color, width, false, start)
                        .apply { path.moveTo(start.x, start.y) }
                add(
                    last.copy(
                        path = Path().apply {
                            addPath(last.path)
                            lineTo(end.x, end.y)
                        }
                    )
                )
            }
        )
    }
}

