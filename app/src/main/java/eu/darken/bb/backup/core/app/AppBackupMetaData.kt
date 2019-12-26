package eu.darken.bb.backup.core.app

import android.content.Context
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.TypeMissMatchException
import eu.darken.bb.processor.core.mm.Props
import java.util.*

@Keep
@JsonClass(generateAdapter = true)
data class AppBackupMetaData(
        override val backupId: Backup.Id,
        override val createdAt: Date = Date()
) : Backup.MetaData {

    override fun getItemLabel(context: Context, spec: BackupSpec, props: Props): Pair<String?, String> {
        spec as AppBackupSpec

        return if (props.label != null && props.originalPath != null) {
            Pair(props.label, props.originalPath!!.userReadablePath(context))
        } else if (props.label != null) {
            Pair(null, props.label!!)
        } else {
            Pair(context.getString(R.string.general_original_path_label), props.originalPath!!.userReadablePath(context))
        }
    }

    override var backupType: Backup.Type
        get() = Backup.Type.APP
        set(value) {
            TypeMissMatchException.check(value, backupType)
        }
}