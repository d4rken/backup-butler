package eu.darken.bb.storage.core

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.storage.core.local.LocalStorageConfig
import eu.darken.bb.storage.core.local.LocalStorageRef
import io.reactivex.Observable
import kotlinx.android.parcel.Parcelize
import java.util.*

interface Storage {
    enum class Type(
            @Transient @DrawableRes val iconRes: Int,
            @Transient @StringRes val labelRes: Int,
            @Transient @StringRes val descriptionRes: Int
    ) {
        LOCAL(R.drawable.ic_sd_storage, R.string.repo_type_local_storage_label, R.string.repo_type_local_storage_desc),
        SAF(R.drawable.ic_sd_storage, R.string.repo_type_saf_storage_label, R.string.repo_type_saf_storage_desc);
    }

    fun info(): Observable<StorageInfo>

    fun content(): Observable<Collection<Content>>

    fun details(content: Content, backupId: Backup.Id): Observable<Backup.Details>

    fun load(content: Content, backupId: Backup.Id): Backup

    fun save(backup: Backup): Pair<Content, Versioning.Version>

    fun remove(content: Content, backupId: Backup.Id? = null): Boolean

    interface Content {
        val storageId: Id
        val backupSpec: BackupSpec
        val versioning: Versioning
    }

    @Parcelize
    data class Id(val id: UUID = UUID.randomUUID()) : Parcelable {
        override fun toString(): String = "StorageId($id)"
    }

    interface Factory {
        fun isCompatible(storageRef: Ref): Boolean

        fun create(storageRef: Ref, progressClient: Progress.Client?): Storage
    }

    interface Ref {
        companion object {
            val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<Ref> = PolymorphicJsonAdapterFactory.of(Ref::class.java, "storageType")
                    .withSubtype(LocalStorageRef::class.java, Type.LOCAL.name)
        }

        val storageId: Id
        val storageType: Type
    }

    interface Config {
        companion object {
            val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<Config> = PolymorphicJsonAdapterFactory.of(Config::class.java, "storageType")
                    .withSubtype(LocalStorageConfig::class.java, Type.LOCAL.name)
        }

        val label: String
        val storageId: Id
        val storageType: Type
    }
}

