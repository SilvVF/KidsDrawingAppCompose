package my.packlol.kidsdrawingapp.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import my.packlol.kidsdrawingapp.drawing.DrawingAction


@Composable
fun DrawingBottomBar(
    modifier: Modifier,
    selectedColor: Color,
    colorBoxClick: () -> Unit,
    widthBoxClick: () -> Unit,
    imageIconClick: () -> Unit,
    hideImageIconClick: () -> Unit,
    saveImageIconClick: () -> Unit,
    actionSink: (DrawingAction) -> Unit
) {
    BottomAppBar(
        modifier = modifier
    ){
        IconButton(
            onClick = { actionSink(DrawingAction.Undo) }
        ) {
            Icon(
                imageVector = Icons.Default.Undo,
                contentDescription = null
            )
        }
        IconButton(
            onClick = { actionSink(DrawingAction.Redo)  }
        ) {
            Icon(
                imageVector = Icons.Default.Redo,
                contentDescription = null
            )
        }
        IconButton(
            onClick = { imageIconClick()  }
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null
            )
        }
        IconButton(
            onClick = { hideImageIconClick()  }
        ) {
            Icon(
                imageVector = Icons.Default.HideImage,
                contentDescription = null
            )
        }
        IconButton(
            onClick = { saveImageIconClick()  }
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null
            )
        }
        Box(modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(selectedColor)
            .clickable { colorBoxClick() }
        )
        Box(modifier = Modifier
            .height(32.dp)
            .width(64.dp)
            .clickable { widthBoxClick() }
            .drawWithCache {
                onDrawBehind {
                    val size = this.size
                    drawPath(
                        color = Color.Black,
                        path = Path().apply {
                            moveTo(0f, size.height)
                            lineTo(size.width, size.height)
                            lineTo(size.width, 0f)
                            lineTo(0f, size.height)
                        }
                    )
                }
            }
        )
    }
}