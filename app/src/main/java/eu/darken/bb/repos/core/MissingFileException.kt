package eu.darken.bb.repos.core

import android.content.Context
import eu.darken.bb.R
import eu.darken.bb.common.LocalizedError
import eu.darken.bb.common.file.SFile

class MissingFileException(private val file: SFile) : IllegalArgumentException(), LocalizedError {

    override fun getLocalizedErrorMessage(context: Context): String {
        return context.resources.getString(R.string.error_message_missing_file_x, file.path)
    }

}