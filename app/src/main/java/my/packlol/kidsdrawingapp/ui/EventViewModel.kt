package my.packlol.kidsdrawingapp.ui

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow

abstract class EventViewModel<EVENT>: ViewModel() {

    protected val mutableEvents = Channel<EVENT>()

    val events = mutableEvents.receiveAsFlow()
}

/**
 * Observe [EventViewModel.events] in a Compose [LaunchedEffect].
 * @param lifecycleState [Lifecycle.State] in which [event] block runs.
 * [orbit_Impl](https://github.com/orbit-mvi/orbit-mvi/blob/main/orbit-compose/src/main/kotlin/org/orbitmvi/orbit/compose/ContainerHostExtensions.kt)
 */
@SuppressLint("ComposableNaming")
@Composable
fun <EVENT> EventViewModel<EVENT>.collectEvents(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    event: (suspend (event: EVENT) -> Unit)
) {
    val sideEffectFlow = events
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(sideEffectFlow, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(lifecycleState) {
            sideEffectFlow.collect { event(it) }
        }
    }
}