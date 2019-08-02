package eu.darken.bb.storage.core

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import eu.darken.bb.R
import eu.darken.bb.backups.core.Backup
import eu.darken.bb.backups.core.BackupId
import io.reactivex.Observable

interface BackupStorage {
    enum class Type(
            @Transient @DrawableRes val iconRes: Int,
            @Transient @StringRes val labelRes: Int,
            @Transient @StringRes val descriptionRes: Int
    ) {
        LOCAL(R.drawable.ic_sd_storage, R.string.repo_type_local_storage_label, R.string.repo_type_local_storage_desc),
        SAF(R.drawable.ic_sd_storage, R.string.repo_type_saf_storage_label, R.string.repo_type_saf_storage_desc);
    }

    fun info(): Observable<StorageInfo>

    fun getAll(): Collection<BackupReference>

    fun load(backupReference: BackupReference, backupId: BackupId): Backup

    fun save(backup: Backup): BackupReference

    fun remove(backupReference: BackupReference): Boolean

    interface Factory {
        fun isCompatible(storageRef: StorageRef): Boolean

        fun create(storageRef: StorageRef): BackupStorage
    }
}

