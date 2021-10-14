package eu.darken.bb.common.files.core

import android.content.Context
import eu.darken.bb.R
import eu.darken.bb.common.errors.HasLocalizedError
import eu.darken.bb.common.errors.LocalizedError
import java.io.File
import java.io.IOException

open class PathException(
    val path: APath,
    message: String = "Error during access.",
    cause: Throwable? = null
) : IOException("$message <-> ${path.path}", cause)

class ReadException(
    path: APath,
    message: String = "Can't read from path.",
    cause: Throwable? = null
) : PathException(path, message, cause), HasLocalizedError {

    constructor(file: File) : this(RawPath.build(file))

    override fun getLocalizedError(context: Context) = LocalizedError(
        throwable = this,
        label = "ReadException",
        description = context.getString(R.string.general_error_cant_access_msg, path)
    )
}

class WriteException(
    path: APath,
    message: String = "Can't write to path.",
    cause: Throwable? = null
) : PathException(path, message, cause), HasLocalizedError {

    constructor(file: File) : this(RawPath.build(file))

    override fun getLocalizedError(context: Context) = LocalizedError(
        throwable = this,
        label = "WriteException",
        description = context.getString(R.string.general_error_cant_access_msg, path)
    )
}