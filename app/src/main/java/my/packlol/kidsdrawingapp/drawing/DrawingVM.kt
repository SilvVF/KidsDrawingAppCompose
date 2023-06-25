package my.packlol.kidsdrawingapp.drawing

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

    private val lineData = MutableStateFlow<List<DragData>>(listOf())

    val dragData = lineData
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    fun handleAction(action: DrawingAction) = viewModelScope.launch {
        when(action) {
            is DrawingAction.SaveImage -> {
                    mutableEvents.send(
                        DrawingEvent.Message("Starting download")
                    )
                    runCatching {
                        imageSaver.saveImage(
                            view = action.view,
                            height = action.clipHeight
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
            is DrawingAction.OnDrag -> {
                updateDragData(
                    action.start,
                    action.end,
                    action.color,
                    action.width
                )
            }
            DrawingAction.Redo -> {
                    deletedLine.takeIf { it.isNotEmpty() }
                        ?.pop()
                        ?.let { data ->
                            lineData.getAndUpdate {
                                it + data
                            }
                        }
                        ?: mutableEvents.send(
                            DrawingEvent.Error("no lines to replace")
                        )
            }
            DrawingAction.Undo -> {
                    lineData.getAndUpdate { data ->
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
            DrawingAction.DragEnded -> Unit
            is DrawingAction.DragStarted -> {
                lineData.emit(
                    buildList {
                        addAll(lineData.value)
                        add(
                            DragData(
                                Path().apply {
                                   moveTo(action.start.x, action.start.y)
                                },
                                action.color,
                                action.width,
                                false,
                                action.start
                            )
                        )
                    }
                )
            }
            is DrawingAction.OnTap -> {
                lineData.emit(
                    buildList {
                        addAll(lineData.value)
                        add(
                            DragData(
                                circle = true,
                                start = action.offset,
                                path = Path(),
                                color = action.color,
                                width = action.width
                            )
                        )
                }
                )
            }
            DrawingAction.ClearDrawing -> {
                lineData.emit(emptyList())
            }
        }
    }

    private suspend fun updateDragData(
        start: Offset,
        end: Offset,
        color: Color,
        width: Float
    )  {
        val data = lineData.value
        lineData.emit(
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

