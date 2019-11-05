package eu.darken.bb.common

import android.content.Context
import eu.darken.bb.R
import eu.darken.bb.common.file.APath
import eu.darken.bb.common.file.RawPath
import java.io.File
import java.io.IOException

class ReadException(val path: APath)
    : IOException("Can't read $path."), LocalizedError {

    constructor(file: File) : this(RawPath.build(file))

    override fun getLocalizedErrorMessage(context: Context): String {
        return context.getString(R.string.general_error_cant_access_msg, path)
    }

}