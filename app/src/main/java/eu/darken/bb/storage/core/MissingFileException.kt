package eu.darken.bb.storage.core

import android.content.Context
import eu.darken.bb.R
import eu.darken.bb.common.LocalizedError
import eu.darken.bb.common.file.AFile

class MissingFileException(private val file: AFile) : IllegalArgumentException(), LocalizedError {

    override fun getLocalizedErrorMessage(context: Context): String {
        return context.resources.getString(R.string.error_message_cant_find_x, file.path)
    }

}