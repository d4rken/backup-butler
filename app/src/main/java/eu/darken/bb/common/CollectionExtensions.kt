package eu.darken.bb.common

fun <T> Collection<T>.replace(newValue: T, block: (T) -> Boolean): Collection<T> {
    return map {
        if (block(it)) newValue else it
    }
}