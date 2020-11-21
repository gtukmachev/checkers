package tga.checkers.exts

import java.util.*

fun <T> linkedListOf(vararg elements : T) = LinkedList<T>().apply { addAll(elements) }
