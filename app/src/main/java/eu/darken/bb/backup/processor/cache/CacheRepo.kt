package eu.darken.bb.backup.processor.cache

import eu.darken.bb.AppComponent
import eu.darken.bb.backup.backups.BackupId
import eu.darken.bb.common.file.SFile
import javax.inject.Inject

@AppComponent.Scope
class CacheRepo @Inject constructor(

) {

    fun create(backupId: BackupId): CacheRef {
        TODO("not implemented")
    }

    fun getAll(backupId: BackupId): Collection<SFile> {
        TODO()
    }

    fun removeAll(backupId: BackupId) {
        TODO()
    }

}