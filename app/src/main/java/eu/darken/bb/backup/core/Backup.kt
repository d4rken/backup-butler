package eu.darken.bb.backup.core

import android.content.Context
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import eu.darken.bb.R
import eu.darken.bb.backup.core.app.AppBackupMetaData
import eu.darken.bb.backup.core.files.FilesBackupMetaData
import eu.darken.bb.common.IdType
import eu.darken.bb.common.OptInfo
import eu.darken.bb.common.moshi.MyPolymorphicJsonAdapterFactory
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
            val spec: BackupSpec,
            val metaData: MetaData,
            val data: Map<String, Collection<MMRef>>
    ) {
        @Transient val specId = spec.specId
        @Transient val backupId = metaData.backupId
    }

    interface MetaData {
        val backupId: Id
        val backupType: Type
        val createdAt: Date

        fun getItemLabel(context: Context, spec: BackupSpec, props: MMRef.Props): String

        companion object {
            val MOSHI_FACTORY: MyPolymorphicJsonAdapterFactory<MetaData> = MyPolymorphicJsonAdapterFactory.of(MetaData::class.java, "backupType")
                    .withSubtype(AppBackupMetaData::class.java, Type.APP.name)
                    .withSubtype(FilesBackupMetaData::class.java, Type.FILES.name)
                    .skipLabelSerialization()
        }
    }

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
    data class Id(override val value: UUID = UUID.randomUUID()) : Parcelable, IdType<Id> {

        constructor(id: String) : this(UUID.fromString(id))

        @IgnoredOnParcel @Transient override val idString = value.toString()

        // TODO test this
        // TODO Test serialization with this as map key
        override fun compareTo(other: Id): Int = value.compareTo(other.value)

        override fun toString(): String = "BackupId($idString)"
    }

    data class Target(
            val storageId: Storage.Id,
            val backupSpecId: BackupSpec.Id,
            val backupId: Id,
            val backupType: Type
    )

    data class Info(
            val storageId: Storage.Id,
            val spec: BackupSpec,
            val metaData: MetaData
    ) {
        @Transient val backupId: Id = metaData.backupId
        @Transient val specId: BackupSpec.Id = spec.specId
        @Transient val backupType: Type = metaData.backupType

    }

    data class InfoOpt(
            val storageId: Storage.Id,
            val specId: BackupSpec.Id,
            val backupId: Id,
            override val info: Info? = null,
            override val error: Throwable? = null
    ) : OptInfo<Info> {
        constructor(info: Info)
                : this(info.storageId, info.specId, info.backupId, info)
    }

    data class ContentInfo(
            val storageId: Storage.Id,
            val spec: BackupSpec,
            val metaData: MetaData,
            val items: Collection<Entry>
    ) {
        @Transient val backupId: Id = metaData.backupId
        @Transient val specId: BackupSpec.Id = spec.specId
        @Transient val backupType: Type = metaData.backupType

        interface Entry {
            fun getLabel(context: Context): String
        }

        data class PropsEntry(
                val spec: BackupSpec,
                val metaData: MetaData,
                val props: MMRef.Props) : Entry {

            override fun getLabel(context: Context): String = metaData.getItemLabel(context, spec, props)
        }

    }

    data class ContentOpt(
            val storageId: Storage.Id,
            val specId: BackupSpec.Id,
            val backupId: Id,
            override val info: ContentInfo?,
            override val error: Throwable?
    ) : OptInfo<ContentInfo>
}
