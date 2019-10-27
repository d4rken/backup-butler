package eu.darken.bb.common

import android.content.Context
import eu.darken.bb.R
import eu.darken.bb.common.file.APath
import eu.darken.bb.common.file.SimplePath
import java.io.File
import java.io.IOException

class ReadException(val path: APath)
    : IOException("Can't read $path."), LocalizedError {

    constructor(file: File) : this(SimplePath.build(file))

    override fun getLocalizedErrorMessage(context: Context): String {
        return context.getString(R.string.error_msg_cant_access, path)
    }

}