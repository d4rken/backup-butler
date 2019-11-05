package eu.darken.bb.common.file.picker

import android.content.Context
import eu.darken.bb.R
import eu.darken.bb.common.LocalizedError

class EmptyResultException
    : IllegalStateException("The result was empty."), LocalizedError {

    override fun getLocalizedErrorMessage(context: Context): String {
        return context.getString(R.string.general_error_empty_result_msg)
    }

}