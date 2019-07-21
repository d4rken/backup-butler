package eu.darken.bb.backup.repos

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import eu.darken.bb.R
import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.backups.BackupId
import io.reactivex.Observable

interface BackupRepo {
    enum class Type(
            @DrawableRes val typeIcon: Int, @StringRes val typeLabel: Int
    ) {
        LOCAL_STORAGE(R.drawable.ic_sd_storage, R.string.repo_type_label_local_storage);

    }

    fun status(): Observable<RepoInfo>

    fun getAll(): Collection<BackupReference>

    fun load(backupReference: BackupReference, backupId: BackupId): Backup

    fun save(backup: Backup): BackupReference

    fun remove(backupReference: BackupReference): Boolean

    interface Factory {
        fun isCompatible(repoReference: RepoReference): Boolean

        fun create(repoReference: RepoReference): BackupRepo
    }
}

