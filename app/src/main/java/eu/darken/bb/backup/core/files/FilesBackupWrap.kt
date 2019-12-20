package eu.darken.bb.backup.core.files

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupWrap
import eu.darken.bb.processor.core.mm.MMRef

class FilesBackupWrap : BackupWrap<FilesBackupSpec> {

    constructor(backup: Backup.Unit) : super(backup)

    constructor(config: FilesBackupSpec, backupId: Backup.Id)
            : super(backupId, config)

    override fun buildMeta(backupId: Backup.Id): Backup.MetaData {
        return FilesBackupMetaData(backupId)
    }

    var files: Collection<MMRef>
        get() = data[""] ?: emptyList()
        set(value) {
            data[""] = value
        }

}
