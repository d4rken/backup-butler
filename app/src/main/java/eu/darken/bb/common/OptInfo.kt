package eu.darken.bb.common

interface OptInfo<T> {
    val info: T?
    val error: Throwable?

    val isFinished: Boolean
        get() = info != null || error != null
}