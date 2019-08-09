package eu.darken.bb.storage.core

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.backup.core.Backup
import java.util.*

interface Versioning {
    val versioningType: Type
    val versions: List<Version>

    fun getVersion(backupId: Backup.Id): Version?

    enum class Type {
        SIMPLE
    }

    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<Versioning> = PolymorphicJsonAdapterFactory.of(Versioning::class.java, "versioningType")
                .withSubtype(SimpleVersioning::class.java, Type.SIMPLE.name)
    }

    interface Version {
        val backupId: Backup.Id
        val createdAt: Date
    }

}