package tga.checkers.web.rest.errors


data class FieldErrorVM(
    val objectName: String,
    val field: String,
    val message: String
)
