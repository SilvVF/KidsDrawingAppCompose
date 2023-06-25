package my.packlol.kidsdrawingapp.drawing

import android.view.View
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

sealed interface DrawingAction {

    object Undo: DrawingAction

    object Redo: DrawingAction

    data class OnDrag(
        val start: Offset,
        val end: Offset,
        val color: Color,
        val width: Float
    ): DrawingAction

    object DragEnded: DrawingAction

    data class DragStarted(
        val start: Offset,
        val color: Color,
        val width: Float
    ): DrawingAction

    data class OnTap(
        val offset: Offset,
        val color: Color,
        val width: Float
    ): DrawingAction

    data class SaveImage(val view: View, val clipHeight: Int): DrawingAction
}