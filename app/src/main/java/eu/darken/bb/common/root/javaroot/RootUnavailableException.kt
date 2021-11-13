package eu.darken.bb.common.root.javaroot

import android.content.Context
import androidx.annotation.StringRes
import eu.darken.bb.R
import eu.darken.bb.common.error.HasLocalizedError
import eu.darken.bb.common.error.LocalizedError
import eu.darken.bb.common.root.javaroot.internal.RootException

class RootUnavailableException(
    message: String,
    cause: Throwable? = null,
    @StringRes val errorMsgRes: Int = R.string.error_root_unavailable
) : RootException(message, cause), HasLocalizedError {

    override fun getLocalizedError(context: Context) = LocalizedError(
        throwable = this,
        label = "RootUnavailableException",
        description = context.getString(errorMsgRes)
    )
}