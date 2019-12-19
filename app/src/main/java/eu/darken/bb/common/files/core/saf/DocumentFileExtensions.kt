package eu.darken.bb.common.files.core.saf

import android.content.ContentResolver
import android.os.ParcelFileDescriptor
import android.system.Os
import android.system.StructStat
import androidx.documentfile.provider.DocumentFile
import eu.darken.bb.common.files.core.Ownership
import eu.darken.bb.common.files.core.Permissions
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
                Os.fchmod(pfd.fileDescriptor, permissions.mode)
                true
            } catch (e: Exception) {
                Timber.w(e, "setPermissions(permissions=%s) failed on %s", permissions, this)
                false
            }
        }

internal fun DocumentFile.setOwnership(contentResolver: ContentResolver, ownership: Ownership): Boolean =
        openParcelFileDescriptor(contentResolver, FileMode.WRITE).use { pfd ->
            try {
                Os.fchown(pfd.fileDescriptor, ownership.userId.toInt(), ownership.groupId.toInt())
                true
            } catch (e: Exception) {
                Timber.w(e, "setOwnership(ownership=%s) failed on %s", ownership, this)
                false
            }
        }