package eu.darken.bb.common

fun <T> Collection<T>.replace(newValue: T, block: (T) -> Boolean): Collection<T> {
    return map {
        if (block(it)) newValue else it
    }
}

fun <T> MutableCollection<T>.addNotNull(value: T?): Boolean {
    return value?.let { add(it) } ?: false
}

suspend inline fun <T, R : Comparable<R>> Iterable<T>.sortedBySuspending(
    crossinline selector: suspend (T) -> R?
): List<T> = this
    .map { it to selector(it) }
    .sortedBy { it.second }
    .map { it.first }

suspend inline fun <T, R : Comparable<R>> Iterable<T>.sortedByDescendingSuspending(
    crossinline selector: suspend (T) -> R?
): List<T> = this
    .map { it to selector(it) }
    .sortedByDescending { it.second }
    .map { it.first }