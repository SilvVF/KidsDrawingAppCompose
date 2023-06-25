package my.packlol.kidsdrawingapp.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController


@Composable
fun ColorPickerPopup(
    initialColor: Color,
    visible: Boolean,
    onDismiss: () -> Unit,
    onColorChange: (Color) -> Unit
) {

    val controller = rememberColorPickerController()

    if (visible) {
        Popup(
            alignment = Alignment.Center,
            onDismissRequest = onDismiss
        ) {
            HsvColorPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
                    .padding(10.dp),
                initialColor = initialColor,
                controller = controller,
                onColorChanged = { colorEnvelope: ColorEnvelope ->
                    if (colorEnvelope.fromUser) {
                        onColorChange(colorEnvelope.color)
                    }
                }
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null
                )
            }
        }
    }
}