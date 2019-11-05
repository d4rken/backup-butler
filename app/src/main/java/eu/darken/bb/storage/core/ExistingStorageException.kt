package eu.darken.bb.storage.core

import android.content.Context
import eu.darken.bb.R
import eu.darken.bb.common.LocalizedError
import eu.darken.bb.common.file.APath

class ExistingStorageException(val path: APath)
    : Exception(), LocalizedError {

    override fun getLocalizedErrorMessage(context: Context): String {
        return context.getString(R.string.storage_existing_storage_in_path_msg, path.userReadablePath(context))
    }

}