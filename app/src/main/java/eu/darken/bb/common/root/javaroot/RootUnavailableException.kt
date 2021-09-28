package eu.darken.bb.common.root.javaroot

import android.content.Context
import androidx.annotation.StringRes
import eu.darken.bb.R
import eu.darken.bb.common.LocalizedError
import eu.darken.bb.common.root.javaroot.internal.RootException

class RootUnavailableException(
    message: String,
    cause: Throwable? = null,
    @StringRes val errorMsgRes: Int = R.string.error_root_unavailable
) : RootException(message, cause), LocalizedError {

    override fun getLocalizedErrorMessage(context: Context): String = context.getString(errorMsgRes)

}