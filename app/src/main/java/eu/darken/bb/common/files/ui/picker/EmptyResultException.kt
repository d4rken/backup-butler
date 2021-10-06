package eu.darken.bb.common.files.ui.picker

import android.content.Context
import eu.darken.bb.R
import eu.darken.bb.common.HasLocalizedError
import eu.darken.bb.common.LocalizedError

class EmptyResultException
    : IllegalStateException("The result was empty."), HasLocalizedError {

    override fun getLocalizedError(context: Context) = LocalizedError(
        throwable = this,
        label = "EmptyResultException",
        description = context.getString(R.string.general_error_empty_result_msg)
    )
}