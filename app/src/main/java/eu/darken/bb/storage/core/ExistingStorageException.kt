package eu.darken.bb.storage.core

import android.content.Context
import eu.darken.bb.R
import eu.darken.bb.common.error.HasLocalizedError
import eu.darken.bb.common.error.LocalizedError
import eu.darken.bb.common.files.core.APath

class ExistingStorageException(val path: APath) : Exception(), HasLocalizedError {

    override fun getLocalizedError(context: Context) = LocalizedError(
        throwable = this,
        label = "ExistingStorageException",
        description = context.getString(R.string.storage_existing_storage_in_path_msg, path.userReadablePath(context))
    )
}