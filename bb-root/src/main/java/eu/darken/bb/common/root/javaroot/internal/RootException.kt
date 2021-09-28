package eu.darken.bb.common.root.javaroot.internal

open class RootException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)