package eu.darken.bb.backup.core.app

import android.content.Context
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.processor.core.mm.MMRef
import java.util.*

data class AppBackupMetaData(
        override val backupId: Backup.Id,
        override val createdAt: Date = Date()
) : Backup.MetaData {

    override fun getItemLabel(context: Context, spec: BackupSpec, props: MMRef.Props): String {
        TODO("not implemented")
//        return props.originalPath.path.replace(path.path, "")
    }

    // TODO test serialization

    override var backupType: Backup.Type
        get() = Backup.Type.APP
        set(value) {}
}