package eu.darken.bb.backup.core

import android.content.Context
import android.os.Parcelable
import eu.darken.bb.backup.core.app.AppBackupSpec
import eu.darken.bb.backup.core.files.FilesBackupSpec
import eu.darken.bb.common.OptInfo
import eu.darken.bb.common.moshi.MyPolymorphicJsonAdapterFactory
import eu.darken.bb.storage.core.Storage
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

interface BackupSpec {
    val backupType: Backup.Type
    val specId: Id
    val revisionLimit: Int

    fun getLabel(context: Context): String

    @Parcelize
    data class Id(val value: String) : Parcelable {

        @IgnoredOnParcel @Transient val idString = value

        override fun toString(): String = "Identifier($idString)"
    }

    data class Target(
            val storageId: Storage.Id,
            val backupSpecId: Id
    )

    companion object {
        val MOSHI_FACTORY: MyPolymorphicJsonAdapterFactory<BackupSpec> = MyPolymorphicJsonAdapterFactory.of(BackupSpec::class.java, "backupType")
                .withSubtype(AppBackupSpec::class.java, Backup.Type.APP.name)
                .withSubtype(FilesBackupSpec::class.java, Backup.Type.FILES.name)
                .skipLabelSerialization()
    }

    interface Info {
        val storageId: Storage.Id
        val backupSpec: BackupSpec
        val backups: Collection<Backup.MetaData>

        val specId: Id
            get() = backupSpec.specId
    }

    data class InfoOpt(
            val storageId: Storage.Id,
            val specId: Id,
            override val info: Info? = null,
            override val error: Throwable? = null
    ) : OptInfo<Info> {
        constructor(info: Info)
                : this(info.storageId, info.specId, info)
    }

}