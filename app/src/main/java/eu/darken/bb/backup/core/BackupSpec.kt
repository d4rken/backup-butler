package eu.darken.bb.backup.core

import android.content.Context
import android.os.Parcelable
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.backup.core.app.AppBackupSpec
import eu.darken.bb.backup.core.file.FileBackupSpec
import kotlinx.android.parcel.Parcelize

interface BackupSpec {
    val backupType: Backup.Type
    val specId: Id

    fun getLabel(context: Context): String

    @Parcelize
    data class Id(val value: String) : Parcelable {
        override fun toString(): String = "Identifier($value)"
    }

    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<BackupSpec> = PolymorphicJsonAdapterFactory.of(BackupSpec::class.java, "backupType")
                .withSubtype(AppBackupSpec::class.java, Backup.Type.APP.name)
                .withSubtype(FileBackupSpec::class.java, Backup.Type.FILE.name)
    }

}