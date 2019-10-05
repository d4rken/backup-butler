package eu.darken.bb.storage.core

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.OptInfo
import eu.darken.bb.common.file.APath
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.storage.core.local.LocalStorageConfig
import eu.darken.bb.storage.core.local.LocalStorageRef
import eu.darken.bb.storage.core.saf.SAFStorageConfig
import eu.darken.bb.storage.core.saf.SAFStorageRef
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.android.parcel.Parcelize
import java.util.*

interface Storage : Progress.Host {
    enum class Type(
            @Transient @DrawableRes val iconRes: Int,
            @Transient @StringRes val labelRes: Int,
            @Transient @StringRes val descriptionRes: Int
    ) {
        LOCAL(R.drawable.ic_sd_storage, R.string.repo_type_local_storage_label, R.string.repo_type_local_storage_desc),
        SAF(R.drawable.ic_storage, R.string.repo_type_saf_storage_label, R.string.repo_type_saf_storage_desc);
    }

    val storageId: Id
        get() = storageConfig.storageId

    val storageConfig: Config

    fun info(): Observable<Info>

    fun items(): Observable<Collection<BackupSpec.Info>>

    fun items(vararg specIds: BackupSpec.Id): Observable<Collection<BackupSpec.Info>>

    fun content(specId: BackupSpec.Id, backupId: Backup.Id): Observable<Backup.Content>

    fun load(specId: BackupSpec.Id, backupId: Backup.Id): Backup.Unit

    fun save(backup: Backup.Unit): Pair<BackupSpec.Info, Versioning.Version>

    fun remove(specId: BackupSpec.Id, backupId: Backup.Id? = null): Single<BackupSpec.Info>

    fun wipe(): Completable

    fun detach(): Completable

    @Parcelize
    data class Id(val id: UUID = UUID.randomUUID()) : Parcelable {
        override fun toString(): String = "StorageId($id)"
    }

    interface Factory<T : Storage> {
        fun create(storageRef: Ref, storageConfig: Config): T
    }

    interface Ref {
        companion object {
            val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<Ref> = PolymorphicJsonAdapterFactory.of(Ref::class.java, "storageType")
                    .withSubtype(LocalStorageRef::class.java, Type.LOCAL.name)
                    .withSubtype(SAFStorageRef::class.java, Type.SAF.name)
        }

        val path: APath
        val storageId: Id
        val storageType: Type
    }

    interface Config {
        companion object {
            val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<Config> = PolymorphicJsonAdapterFactory.of(Config::class.java, "storageType")
                    .withSubtype(LocalStorageConfig::class.java, Type.LOCAL.name)
                    .withSubtype(SAFStorageConfig::class.java, Type.SAF.name)
        }

        val label: String
        val storageId: Id
        val storageType: Type
    }

    data class Info(
            val storageId: Id,
            val storageType: Type,
            val config: Config? = null,
            val status: Status? = null,
            val error: Throwable? = null
    ) {

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
    }
}

