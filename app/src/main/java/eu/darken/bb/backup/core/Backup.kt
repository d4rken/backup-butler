package eu.darken.bb.backup.core

import android.content.Context
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.squareup.moshi.JsonClass
import eu.darken.bb.R
import eu.darken.bb.backup.core.app.AppBackupMetaData
import eu.darken.bb.backup.core.files.FilesBackupMetaData
import eu.darken.bb.common.IdType
import eu.darken.bb.common.OptInfo
import eu.darken.bb.common.SharedHolder
import eu.darken.bb.common.moshi.MyPolymorphicJsonAdapterFactory
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.Props
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.task.core.results.LogEvent
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

interface Backup {
    interface Endpoint : Progress.Host, SharedHolder.HasKeepAlive<Any> {
        fun backup(spec: BackupSpec, logListener: ((LogEvent) -> kotlin.Unit)?): Unit
    }

    data class Unit(
            val spec: BackupSpec,
            val metaData: MetaData,
            val data: Map<String, Collection<MMRef>>
    ) {
        @Transient val specId = spec.specId
        @Transient val backupId = metaData.backupId
    }

    @Keep
    interface MetaData {
        val backupId: Id
        val backupType: Type
        val createdAt: Date

        fun getItemLabel(context: Context, spec: BackupSpec, props: Props): Pair<String?, String>

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
        APP(R.drawable.ic_apps, R.string.backup_type_app_label, R.string.backup_type_app_desc),
        FILES(R.drawable.ic_folder_onsurface, R.string.backup_type_files_label, R.string.backup_type_files_desc);
    }

    @Keep @Parcelize
    @JsonClass(generateAdapter = true)
    data class Id(override val value: UUID = UUID.randomUUID()) : Parcelable, IdType<Id> {

        constructor(id: String) : this(UUID.fromString(id))

        @IgnoredOnParcel @Transient override val idString = value.toString()

        override fun compareTo(other: Id): Int = value.compareTo(other.value)

        override fun toString(): String = "BackupId($idString)"
    }

    @Keep
    @JsonClass(generateAdapter = true)
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
            fun getLabel(context: Context): Pair<String?, String>
        }

        data class PropsEntry(
                val spec: BackupSpec,
                val metaData: MetaData,
                val props: Props
        ) : Entry {

            override fun getLabel(context: Context): Pair<String?, String> = metaData.getItemLabel(context, spec, props)
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
