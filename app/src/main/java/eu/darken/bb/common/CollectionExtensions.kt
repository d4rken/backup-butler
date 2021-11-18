package eu.darken.bb.common

fun <T> Collection<T>.replace(newValue: T, block: (T) -> Boolean): Collection<T> {
    return map {
        if (block(it)) newValue else it
    }
}

fun <T> MutableCollection<T>.addNotNull(value: T?): Boolean {
    return value?.let { add(it) } ?: false
}
