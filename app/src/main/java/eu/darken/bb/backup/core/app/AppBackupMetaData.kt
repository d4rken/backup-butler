package eu.darken.bb.backup.core.app

import android.content.Context
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.TypeMissMatchException
import eu.darken.bb.processor.core.mm.MMRef
import java.util.*

@Keep
@JsonClass(generateAdapter = true)
data class AppBackupMetaData(
        override val backupId: Backup.Id,
        override val createdAt: Date = Date()
) : Backup.MetaData {

    override fun getItemLabel(context: Context, spec: BackupSpec, props: MMRef.Props): String {
        spec as AppBackupSpec
//        TODO("not implemented")
        return props.label
    }

    override var backupType: Backup.Type
        get() = Backup.Type.APP
        set(value) {
            TypeMissMatchException.check(value, backupType)
        }
}