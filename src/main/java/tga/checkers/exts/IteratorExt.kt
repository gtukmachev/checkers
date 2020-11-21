package tga.checkers.exts

fun <T> Iterator<T>.nextOrNull(): T? = when (this.hasNext()) {
    true -> this.next()
    else -> null
}
