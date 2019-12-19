package eu.darken.bb.common.files.core

import eu.darken.bb.backup.core.app.restore.AppRestoreEndpoint
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import timber.log.Timber

fun TarArchiveEntry.toHumanReadableString(): String {
    return "TarArchiveEntry(" +
            "name=${this.name}, " +
            "modified=${this.modTime}, " +
            "uid=${this.longUserId}, gid=${this.longGroupId}," +
            "userName=${this.userName}, groupName=${this.groupName}" +
            ")"
}

fun TarArchiveEntry.getOwnership(): Ownership? {
    return try {
        Ownership(
                longUserId,
                longGroupId,
                if (userName.isNullOrEmpty()) null else userName,
                if (groupName.isNullOrEmpty()) null else groupName
        )
    } catch (e: Exception) {
        Timber.tag(AppRestoreEndpoint.TAG).w(e, "Invalid ownership data for %s", name)
        null
    }
}