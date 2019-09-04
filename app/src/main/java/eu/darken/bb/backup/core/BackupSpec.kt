package eu.darken.bb.backup.core

import android.content.Context
import android.os.Parcelable
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.backup.core.app.AppBackupSpec
import eu.darken.bb.backup.core.files.FilesBackupSpec
import eu.darken.bb.storage.core.Storage
import kotlinx.android.parcel.Parcelize

interface BackupSpec {
    val backupType: Backup.Type
    val specId: Id
    val revisionLimit: Int

    fun getLabel(context: Context): String

    @Parcelize
    data class Id(val value: String) : Parcelable {
        override fun toString(): String = "Identifier($value)"
    }


    data class Target(
            val storageId: Storage.Id,
            val backupSpecId: Id
    )

    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<BackupSpec> = PolymorphicJsonAdapterFactory.of(BackupSpec::class.java, "backupType")
                .withSubtype(AppBackupSpec::class.java, Backup.Type.APP.name)
                .withSubtype(FilesBackupSpec::class.java, Backup.Type.FILES.name)
    }

}