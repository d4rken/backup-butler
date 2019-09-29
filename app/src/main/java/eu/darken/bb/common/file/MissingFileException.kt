package eu.darken.bb.common.file

import android.content.Context
import eu.darken.bb.R
import eu.darken.bb.common.LocalizedError

class MissingFileException(private val path: APath) : IllegalArgumentException(), LocalizedError {

    override fun getLocalizedErrorMessage(context: Context): String {
        return context.resources.getString(R.string.error_message_cant_find_x, path.path)
    }

}