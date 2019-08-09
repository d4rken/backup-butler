package eu.darken.bb.backup.core

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import eu.darken.bb.R
import eu.darken.bb.processor.tmp.TmpRef
import kotlinx.android.parcel.Parcelize
import java.util.*

data class Backup(
        val id: Id,
        val backupType: Type,
        val spec: BackupSpec,
        val data: Map<String, Collection<TmpRef>>
) {

    @Keep
    enum class Type constructor(
            @DrawableRes val iconRes: Int,
            @StringRes val labelRes: Int,
            @StringRes val descriptionRes: Int
    ) {
        APP(R.drawable.ic_apps, R.string.label_backuptype_app, R.string.descr_backuptype_app),
        FILE(R.drawable.ic_folder, R.string.label_backuptype_file, R.string.descr_backuptype_file);
    }

    @Parcelize
    data class Id(val id: UUID = UUID.randomUUID()) : Parcelable {
        override fun toString(): String = "BackupId($id)"
    }

    data class Details(
            val items: Collection<Item>
    )

    interface Item {
        val label: String
    }
}

