package eu.darken.bb.storage.core

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.squareup.moshi.JsonClass
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.IdType
import eu.darken.bb.common.OptInfo
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.moshi.MyPolymorphicJsonAdapterFactory
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.sharedresource.HasSharedResource
import eu.darken.bb.storage.core.local.LocalStorageConfig
import eu.darken.bb.storage.core.local.LocalStorageRef
import eu.darken.bb.storage.core.saf.SAFStorageConfig
import eu.darken.bb.storage.core.saf.SAFStorageRef
import kotlinx.coroutines.flow.Flow
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*

interface Storage : Progress.Host, HasSharedResource<Any> {
    @Keep
    enum class Type(
        @Transient @DrawableRes val iconRes: Int,
        @Transient @StringRes val labelRes: Int,
        @Transient @StringRes val descriptionRes: Int
    ) {
        LOCAL(R.drawable.ic_sd_storage, R.string.storage_type_local_label, R.string.storage_type_local_desc),
        SAF(R.drawable.ic_saf, R.string.storage_type_saf_label, R.string.storage_type_saf_desc);
    }

    val storageId: Id
        get() = storageConfig.storageId

    val storageConfig: Config

    fun info(): Flow<Info>

    fun specInfos(): Flow<Collection<BackupSpec.Info>>

    fun specInfo(specId: BackupSpec.Id): Flow<BackupSpec.Info>

    fun backupInfo(specId: BackupSpec.Id, backupId: Backup.Id): Flow<Backup.Info>

    fun backupContent(specId: BackupSpec.Id, backupId: Backup.Id): Flow<Backup.ContentInfo>

    suspend fun load(specId: BackupSpec.Id, backupId: Backup.Id): Backup.Unit

    suspend fun save(backup: Backup.Unit): Backup.Info

    suspend fun remove(specId: BackupSpec.Id, backupId: Backup.Id? = null): BackupSpec.Info

    suspend fun detach(wipe: Boolean = false)

    @Parcelize @Keep
    @JsonClass(generateAdapter = true)
    data class Id(override val value: UUID = UUID.randomUUID()) : IdType<Id>, Parcelable {

        @IgnoredOnParcel @Transient override val idString = value.toString()

        override fun compareTo(other: Id): Int = value.compareTo(other.value)

        override fun toString(): String = "StorageId($idString)"
    }

    interface Factory<T : Storage> {
        fun create(storageRef: Ref, storageConfig: Config): T
    }

    @Keep
    interface Ref {
        companion object {
            val MOSHI_FACTORY: MyPolymorphicJsonAdapterFactory<Ref> =
                MyPolymorphicJsonAdapterFactory.of(Ref::class.java, "storageType")
                    .withSubtype(LocalStorageRef::class.java, Type.LOCAL.name)
                    .withSubtype(SAFStorageRef::class.java, Type.SAF.name)
                    .skipLabelSerialization()
        }

        val path: APath
        val storageId: Id
        val storageType: Type
    }

    @Keep
    interface Config {
        companion object {
            val MOSHI_FACTORY: MyPolymorphicJsonAdapterFactory<Config> =
                MyPolymorphicJsonAdapterFactory.of(Config::class.java, "storageType")
                    .withSubtype(LocalStorageConfig::class.java, Type.LOCAL.name)
                    .withSubtype(SAFStorageConfig::class.java, Type.SAF.name)
                    .skipLabelSerialization()
        }

        val label: String
        val storageId: Id
        val storageType: Type
        val strategy: Strategy
        val version: Int
    }

    data class Info(
        val storageId: Id,
        val storageType: Type,
        val config: Config? = null,
        val status: Status? = null,
        val error: Throwable? = null
    ) {

        val isFinished: Boolean = error != null || (config != null && status?.isFinished == true)

        data class Status(
            val isReadOnly: Boolean,
            val itemCount: Int,
            val totalSize: Long
        ) {
            val isFinished: Boolean = itemCount != -1 && totalSize != -1L
        }

    }

    data class InfoOpt(
        val storageId: Id,
        override val info: Info?,
        override val error: Throwable? = null
    ) : OptInfo<Info> {
        constructor(storageId: Id) : this(storageId, null)
        constructor(config: Info) : this(config.storageId, config)

        val anyError: Throwable? = info?.error ?: error

        override val isFinished: Boolean = info?.isFinished ?: (error != null)
    }

    @Keep
    interface Strategy {

        val type: Type

        @Keep
        enum class Type(
            @Transient @StringRes val labelRes: Int,
            @Transient @StringRes val descriptionRes: Int
        ) {
            SIMPLE(R.string.storage_strategy_simple_label, R.string.storage_strategy_simple_desc)
        }

        companion object {
            val MOSHI_FACTORY: MyPolymorphicJsonAdapterFactory<Strategy> =
                MyPolymorphicJsonAdapterFactory.of(Strategy::class.java, "type")
                    .withSubtype(SimpleStrategy::class.java, Type.SIMPLE.name)
                    .skipLabelSerialization()

        }
    }
}

