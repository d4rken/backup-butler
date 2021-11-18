package eu.darken.bb.storage.core

import android.content.Context
import androidx.annotation.StringRes
import eu.darken.bb.R
import eu.darken.bb.common.error.HasLocalizedError
import eu.darken.bb.common.error.LocalizedError
import eu.darken.bb.common.files.core.APath

class IllegalStoragePathException(
    path: APath,
    @StringRes val errorMsgRes: Int = R.string.general_error_cant_access_msg
) : IllegalStateException("Illegal storage path: $path"), HasLocalizedError {

    override fun getLocalizedError(context: Context) = LocalizedError(
        throwable = this,
        label = "IllegalStoragePathException",
        description = context.getString(errorMsgRes)
    )
}