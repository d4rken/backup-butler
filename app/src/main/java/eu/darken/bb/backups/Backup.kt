package eu.darken.bb.backups

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import eu.darken.bb.R
import eu.darken.bb.processor.tmp.TmpRef

data class Backup(
        val id: BackupId,
        val backupType: Type,
        val config: BackupConfig,
        val data: Map<String, Collection<TmpRef>>
) {

    @Keep
    enum class Type constructor(
            @DrawableRes val iconRes: Int,
            @StringRes val labelRes: Int
    ) {
        APP(R.drawable.ic_apps, R.string.label_apps),
        FILE(R.drawable.ic_folder, R.string.label_files);
    }
}

