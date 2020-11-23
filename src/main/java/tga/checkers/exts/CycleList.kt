package tga.checkers.exts

data class CycleList<T>(val obj: T, var next: CycleList<T>? = null): Iterable<T> {

    override fun iterator(): Iterator<T> = CycleListIterator(this)

    private class CycleListIterator<T>(val startNode: CycleList<T>): Iterator<T> {

        var currentNode = startNode
        var iterationHasBeenStarted = false

        override fun hasNext() = !iterationHasBeenStarted || currentNode != startNode

        override fun next(): T {
            iterationHasBeenStarted = true

            val result = currentNode
            currentNode = currentNode.next!!

            return result.obj
        }
    }

}

fun <T> Collection<T>.toCyclyList(): CycleList<T>? {
    if (this.isEmpty()) return null

    val iterator = this.iterator()

    var node = CycleList(iterator.next())
    val firstNode = node

    while (iterator.hasNext()) {
        node.next = CycleList(iterator.next())
        node = node.next!!
    }

    node.next = firstNode

    return firstNode
}
