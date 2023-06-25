package my.packlol.kidsdrawingapp.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

data class DragData(
    val path: Path,
    val color: Color,
    val width: Float,
    val circle: Boolean = false,
    val start: Offset = Offset.Zero
)