package eu.darken.bb.backup.core

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import eu.darken.bb.R
import eu.darken.bb.common.OptInfo
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.storage.core.Storage
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

interface Backup {
    interface Endpoint : Progress.Host {
        fun backup(spec: BackupSpec): Unit
    }

    data class Unit(
            val id: Id,
            val spec: BackupSpec,
            val data: Map<String, Collection<MMRef>>
    )

    @Keep
    enum class Type constructor(
            @DrawableRes val iconRes: Int,
            @StringRes val labelRes: Int,
            @StringRes val descriptionRes: Int
    ) {
        APP(R.drawable.ic_apps, R.string.label_backuptype_app, R.string.descr_backuptype_app),
        FILES(R.drawable.ic_folder, R.string.label_backuptype_file, R.string.descr_backuptype_file);
    }

    @Parcelize
    data class Id(val id: UUID = UUID.randomUUID()) : Parcelable {

        @IgnoredOnParcel
        val idString = id.toString()

        override fun toString(): String = "BackupId($idString)"
    }

    data class Target(
            val storageId: Storage.Id,
            val backupSpecId: BackupSpec.Id,
            val backupId: Id
    )

    data class Content(
            val items: Collection<Entry>
    ) {
        interface Entry {
            val label: String
        }
    }

    data class Info(
            val storageId: Storage.Id,
            val specId: BackupSpec.Id,
            val backupId: Id
    )

    data class InfoOpt(
            val storageId: Storage.Id,
            val specId: BackupSpec.Id,
            val backupId: Id,
            override val info: Info?,
            override val error: Throwable?
    ) : OptInfo<Info>
}
