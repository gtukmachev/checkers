package tga.checkers.exts

fun Throwable.shortMsg(): String {
    val errClass = this::class.java.simpleName
    val errMsg = this.message
    return "$errClass: \"$errMsg\""
}
