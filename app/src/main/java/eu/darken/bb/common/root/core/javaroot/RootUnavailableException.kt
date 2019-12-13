package eu.darken.bb.common.root.core.javaroot

import android.content.Context
import eu.darken.bb.R
import eu.darken.bb.common.LocalizedError

class RootUnavailableException(message: String) : Exception(message), LocalizedError {
    override fun getLocalizedErrorMessage(context: Context): String = context.getString(R.string.error_root_unavailable)
}