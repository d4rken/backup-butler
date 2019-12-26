package eu.darken.bb.backup.core.app

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.AString
import eu.darken.bb.common.CAString
import eu.darken.bb.common.PathAString
import eu.darken.bb.common.TypeMissMatchException
import eu.darken.bb.processor.core.mm.Props
import java.util.*

@Keep
@JsonClass(generateAdapter = true)
data class AppBackupMetaData(
        override val backupId: Backup.Id,
        override val createdAt: Date = Date()
) : Backup.MetaData {

    override fun getItemLabeling(spec: BackupSpec, props: Props): Pair<AString, AString> {
        spec as AppBackupSpec

        return if (props.label != null && props.originalPath != null) {
            Pair(CAString(props.label!!), PathAString(props.originalPath!!))
        } else if (props.label != null) {
            Pair(AString.EMPTY, CAString(props.label!!))
        } else {
            Pair(CAString(R.string.general_original_path_label), PathAString(props.originalPath!!))
        }
    }

    override var backupType: Backup.Type
        get() = Backup.Type.APP
        set(value) {
            TypeMissMatchException.check(value, backupType)
        }
}