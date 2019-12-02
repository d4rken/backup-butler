package eu.darken.bb.common.file.core.saf

import android.content.ContentResolver
import android.os.ParcelFileDescriptor
import android.system.Os
import android.system.StructStat
import androidx.documentfile.provider.DocumentFile
import eu.darken.bb.common.file.core.Ownership
import eu.darken.bb.common.file.core.Permissions
import timber.log.Timber
import java.io.IOException


internal enum class FileMode constructor(val value: String) {
    WRITE("w"), READ("r")
}

internal fun DocumentFile.fstat(contentResolver: ContentResolver): StructStat? {
    return try {
        val pfd = openParcelFileDescriptor(contentResolver, FileMode.READ)
        pfd.use { Os.fstat(pfd.fileDescriptor) }
    } catch (e: Exception) {
        Timber.tag(SAFGateway.TAG).w(e, "Failed to fstat SAFPath: %s", this)
        null
    }
}

internal fun DocumentFile.openParcelFileDescriptor(contentResolver: ContentResolver, mode: FileMode): ParcelFileDescriptor {
    val pfd = contentResolver.openFileDescriptor(this.uri, mode.value)
    if (pfd == null) throw IOException("Couldn't open $uri")
    return pfd
}

internal fun DocumentFile.setPermissions(contentResolver: ContentResolver, permissions: Permissions): Boolean =
        openParcelFileDescriptor(contentResolver, FileMode.WRITE).use { pfd ->
            try {
                if (permissions.isValid) {
                    Os.fchmod(pfd.fileDescriptor, permissions.mode)
                    true
                } else {
                    Timber.w("Can't do setPermissions(permissions=%s) with invalid modes on %s", this)
                    false
                }
            } catch (e: Exception) {
                Timber.w(e, "setPermissions(permissions=%s) failed on %s", permissions, this)
                false
            }
        }

internal fun DocumentFile.setOwnership(contentResolver: ContentResolver, ownership: Ownership): Boolean =
        openParcelFileDescriptor(contentResolver, FileMode.WRITE).use { pfd ->
            try {
                if (ownership.isValid) {
                    Os.fchown(pfd.fileDescriptor, ownership.userId, ownership.groupId)
                    true
                } else {
                    Timber.w("Can't do setOwnership(ownership=%s) with invalid ids on %s", this)
                    false
                }
            } catch (e: Exception) {
                Timber.w(e, "setOwnership(ownership=%s) failed on %s", ownership, this)
                false
            }
        }