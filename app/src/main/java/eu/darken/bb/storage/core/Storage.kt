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
import eu.darken.bb.storage.core.local.LocalStorageConfig
import eu.darken.bb.storage.core.local.LocalStorageRef
import eu.darken.bb.storage.core.saf.SAFStorageConfig
import eu.darken.bb.storage.core.saf.SAFStorageRef
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

interface Storage : Progress.Host {
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

    fun info(): Observable<Info>

    fun specInfos(): Observable<Collection<BackupSpec.Info>>

    fun specInfo(specId: BackupSpec.Id): Observable<BackupSpec.Info>

    fun backupInfo(specId: BackupSpec.Id, backupId: Backup.Id): Observable<Backup.Info>

    fun backupContent(specId: BackupSpec.Id, backupId: Backup.Id): Observable<Backup.ContentInfo>

    fun load(specId: BackupSpec.Id, backupId: Backup.Id): Backup.Unit

    fun save(backup: Backup.Unit): Backup.Info

    fun remove(specId: BackupSpec.Id, backupId: Backup.Id? = null): Single<BackupSpec.Info>

    fun detach(wipe: Boolean = false): Completable

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
            val MOSHI_FACTORY: MyPolymorphicJsonAdapterFactory<Ref> = MyPolymorphicJsonAdapterFactory.of(Ref::class.java, "storageType")
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
            val MOSHI_FACTORY: MyPolymorphicJsonAdapterFactory<Config> = MyPolymorphicJsonAdapterFactory.of(Config::class.java, "storageType")
                    .withSubtype(LocalStorageConfig::class.java, Type.LOCAL.name)
                    .withSubtype(SAFStorageConfig::class.java, Type.SAF.name)
                    .skipLabelSerialization()
        }

        val label: String
        val storageId: Id
        val storageType: Type
        val strategy: Strategy
    }

    data class Info(
            val storageId: Id,
            val storageType: Type,
            val config: Config? = null,
            val status: Status? = null,
            val error: Throwable? = null
    ) {

        val isFinished: Boolean = error != null || (config != null && status != null)

        data class Status(
                val isReadOnly: Boolean,
                val itemCount: Int,
                val totalSize: Long
        )

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
            val MOSHI_FACTORY: MyPolymorphicJsonAdapterFactory<Strategy> = MyPolymorphicJsonAdapterFactory.of(Strategy::class.java, "type")
                    .withSubtype(SimpleStrategy::class.java, Type.SIMPLE.name)
                    .skipLabelSerialization()

        }
    }
}

