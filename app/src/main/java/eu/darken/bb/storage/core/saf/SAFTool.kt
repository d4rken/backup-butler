package eu.darken.bb.storage.core.saf

import android.content.ContentResolver
import android.content.Intent
import eu.darken.bb.App
import eu.darken.bb.common.file.SFile
import eu.darken.bb.common.file.UriFile
import timber.log.Timber
import javax.inject.Inject

class SAFTool @Inject constructor(
        private val contentResolver: ContentResolver
) {

    fun takePermission(file: SFile): Boolean {
        file as UriFile
        var permissionTaken = false
        try {
            contentResolver.takePersistableUriPermission(file.uri, RW_FLAGSINT)
            permissionTaken = true
        } catch (e: SecurityException) {
            Timber.tag(TAG).e(e, "Failed to take permission")
            try {
                contentResolver.releasePersistableUriPermission(file.uri, RW_FLAGSINT)
            } catch (ignore: SecurityException) {
            }
        }
        return permissionTaken
    }

    fun releasePermission(file: SFile): Boolean {
        file as UriFile
        contentResolver.releasePersistableUriPermission(file.uri, RW_FLAGSINT)
        return true
    }

    fun getPermissions(): Collection<SFile> {
        return contentResolver.persistedUriPermissions.map { UriFile(SFile.Type.DIRECTORY, it.uri) }
    }

    fun hasPermission(path: SFile): Boolean {
        return getPermissions().contains(path)
    }

    fun createPickerIntent(): Intent {
        val requestIntent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        requestIntent.putExtra("android.content.extra.SHOW_ADVANCED", true)
        return requestIntent
    }

    companion object {
        val TAG = App.logTag("SAF", "Manager")

        const val RW_FLAGSINT = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    }
}