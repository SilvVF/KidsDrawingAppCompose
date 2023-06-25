package my.packlol.kidsdrawingapp

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import my.packlol.kidsdrawingapp.drawing.DrawingAction
import my.packlol.kidsdrawingapp.drawing.DrawingEvent
import my.packlol.kidsdrawingapp.drawing.DrawingVM
import my.packlol.kidsdrawingapp.ui.DrawingTheme
import my.packlol.kidsdrawingapp.ui.collectEvents
import my.packlol.kidsdrawingapp.composables.ColorPickerPopup
import my.packlol.kidsdrawingapp.composables.DrawingBottomBar
import my.packlol.kidsdrawingapp.composables.DrawingCanvas
import my.packlol.kidsdrawingapp.composables.WidthPickerPopup
import org.koin.androidx.compose.koinViewModel


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {

            val vm = koinViewModel<DrawingVM>()
            val dragData by vm.dragData.collectAsState()

            var showColorPicker by remember {
                mutableStateOf(false)
            }
            var showWidthPicker by remember {
                mutableStateOf(false)
            }
            var width by remember {
                mutableStateOf(4f)
            }
            var color by remember {
                mutableStateOf(Color.Black)
            }
            var uri by remember {
                mutableStateOf<Uri?>(null)
            }
            var bottomBarHeight by remember {
                mutableStateOf(0)
            }

            val ctx = LocalContext.current
            val view = LocalView.current

            val snackBarStateHostState = remember{ SnackbarHostState() }

            val photoPicker = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia(),
                onResult = { uri = it }
            )

            vm.collectEvents { event ->
                when (event) {
                    is DrawingEvent.Error -> snackBarStateHostState
                        .showSnackbar(
                            message = event.message,
                            duration = SnackbarDuration.Short
                        )

                    is DrawingEvent.Message ->  snackBarStateHostState
                        .showSnackbar(
                            message = event.message,
                            withDismissAction = true,
                            duration = SnackbarDuration.Short
                        )
                }
            }


            ColorPickerPopup(
                initialColor = color,
                visible = showColorPicker,
                onDismiss = { showColorPicker = false },
                onColorChange = {
                    color = it
                }
            )

            WidthPickerPopup(
                visible = showWidthPicker,
                width = width,
                color = color,
                onDismiss = { showWidthPicker = false },
                onWidthChange = {
                    width = it
                }
            )

            DrawingTheme {
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(hostState = snackBarStateHostState)
                    },
                    bottomBar = {
                       DrawingBottomBar(
                           modifier = Modifier
                               .fillMaxWidth()
                               .onGloballyPositioned {
                                   bottomBarHeight = it.size.height
                               },
                           selectedColor = color,
                           colorBoxClick = {
                               showColorPicker = true
                           },
                           imageIconClick = {
                                photoPicker.launch(
                                   PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                           },
                           widthBoxClick = {
                               showWidthPicker = true
                           },
                           hideImageIconClick = {
                               uri = null
                           },
                           saveImageIconClick = {
                                vm.handleAction(
                                    DrawingAction.SaveImage(
                                        view, bottomBarHeight
                                    )
                                )
                           }
                       ) { action ->
                           vm.handleAction(action)
                       }
                    }
                ) { paddingValues ->
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        Box {
                            uri?.let {
                                AsyncImage(
                                    model = ImageRequest.Builder(ctx)
                                        .data(it)
                                        .crossfade(true)
                                        .build(),
                                    modifier = Modifier.fillMaxSize(),
                                    contentDescription = null
                                )
                            }
                            DrawingCanvas(
                                modifier = Modifier.fillMaxSize(),
                                dragData = dragData,
                                actionSink = vm::handleAction,
                                onTap = { offset ->
                                    vm.handleAction(
                                        DrawingAction.OnTap(
                                            offset = offset,
                                            color = color,
                                            width = width
                                        )
                                    )
                                },
                                onDragStarted = {
                                    vm.handleAction(
                                        DrawingAction.DragStarted(it, color, width)
                                    )
                                },
                                onDraw = { start, end ->
                                    vm.handleAction(
                                        DrawingAction.OnDrag(
                                            start = start,
                                            end = end,
                                            color = color,
                                            width = width
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
