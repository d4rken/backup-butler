package eu.darken.bb.common.root.javaroot.internal

import java.io.IOException

open class RootException(
    message: String,
    cause: Throwable? = null,
) : IOException(message, cause)