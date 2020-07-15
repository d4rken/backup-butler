package eu.darken.bb.storage.core

import android.content.Context
import androidx.annotation.StringRes
import eu.darken.bb.R
import eu.darken.bb.common.LocalizedError
import eu.darken.bb.common.files.core.APath

class IllegalStoragePathException(
        path: APath,
        @StringRes val errorMsgRes: Int = R.string.general_error_cant_access_msg
) : IllegalStateException("Illegal storage path: $path"), LocalizedError {

    override fun getLocalizedErrorMessage(context: Context): String {
        return context.getString(errorMsgRes)
    }

}