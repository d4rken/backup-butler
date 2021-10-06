package eu.darken.bb.backup.core.app

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.CaString
import eu.darken.bb.common.TypeMissMatchException
import eu.darken.bb.common.toCaString
import eu.darken.bb.processor.core.mm.Props
import java.util.*

@Keep
@JsonClass(generateAdapter = true)
data class AppBackupMetaData(
    override val backupId: Backup.Id,
    override val createdAt: Date = Date()
) : Backup.MetaData {

    override fun getItemLabeling(spec: BackupSpec, props: Props): Pair<CaString, CaString> {
        spec as AppBackupSpec

        return if (props.label != null && props.originalPath != null) {
            Pair(props.label!!.toCaString(), props.originalPath!!.toCaString())
        } else if (props.label != null) {
            Pair(CaString.EMPTY, props.label!!.toCaString())
        } else {
            Pair(R.string.general_original_path_label.toCaString(), props.originalPath!!.toCaString())
        }
    }

    override var backupType: Backup.Type
        get() = Backup.Type.APP
        set(value) {
            TypeMissMatchException.check(value, backupType)
        }
}