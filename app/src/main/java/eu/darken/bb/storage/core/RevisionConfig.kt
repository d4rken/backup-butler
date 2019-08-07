package eu.darken.bb.storage.core

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.backup.core.BackupId
import java.util.*

interface RevisionConfig {
    val revisionType: Type
    val revisions: List<Revision>

    fun getRevision(backupId: BackupId): Revision?

    enum class Type {
        SIMPLE
    }

    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<RevisionConfig> = PolymorphicJsonAdapterFactory.of(RevisionConfig::class.java, "revisionType")
                .withSubtype(DefaultRevisionConfig::class.java, Type.SIMPLE.name)
    }

    interface Revision {
        val backupId: BackupId
        val createdAt: Date
    }
}