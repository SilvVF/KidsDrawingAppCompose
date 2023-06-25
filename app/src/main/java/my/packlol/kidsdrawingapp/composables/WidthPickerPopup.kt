package my.packlol.kidsdrawingapp.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import kotlin.math.roundToInt

@Composable
fun WidthPickerPopup(
    visible: Boolean,
    width: Float,
    color: Color,
    onDismiss: () -> Unit,
    onWidthChange: (Float) -> Unit
) {
    if (visible) {
        Popup(
            alignment = Alignment.Center,
            onDismissRequest = onDismiss
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(420.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                    }
                    Text("Line Width")
                }
                Slider(
                    valueRange = 1f..120f,
                    value = width,
                    onValueChange = {
                        onWidthChange(
                            it.roundToInt().toFloat()
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(text = width.toString())
                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = width.dp,
                    color = color
                )
            }
        }
    }
}