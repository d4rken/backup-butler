package eu.darken.bb.common.root.core.javaroot

import android.content.Context
import androidx.annotation.StringRes
import eu.darken.bb.R
import eu.darken.bb.common.LocalizedError

class RootException(
    message: String,
    cause: Throwable? = null,
    @StringRes val errorMsgRes: Int = R.string.error_root_unavailable
) : Exception(message, cause), LocalizedError {

    override fun getLocalizedErrorMessage(context: Context): String = context.getString(errorMsgRes)

}