package my.packlol.kidsdrawingapp.drawing

sealed interface DrawingEvent {

    data class Error(val message: String): DrawingEvent

    data class Message(val message: String): DrawingEvent
}