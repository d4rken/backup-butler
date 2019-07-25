package eu.darken.bb.storage.core

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import eu.darken.bb.R
import eu.darken.bb.backups.Backup
import eu.darken.bb.backups.BackupId
import io.reactivex.Observable

interface BackupStorage {
    enum class Type(
            @Transient @DrawableRes val iconRes: Int,
            @Transient @StringRes val labelRes: Int,
            @Transient @StringRes val descriptionRes: Int
    ) {
        LOCAL_STORAGE(R.drawable.ic_sd_storage, R.string.repo_type_local_storage_label, R.string.repo_type_local_storage_desc);
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

