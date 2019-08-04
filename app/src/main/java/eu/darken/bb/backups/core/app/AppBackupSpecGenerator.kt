package eu.darken.bb.backups.core.app

import android.content.Context
import dagger.Reusable
import eu.darken.bb.backups.core.Backup
import eu.darken.bb.backups.core.BackupSpec
import eu.darken.bb.backups.core.SpecGenerator
import java.util.*
import javax.inject.Inject

@Reusable
class AppBackupSpecGenerator @Inject constructor() : SpecGenerator {
    override fun generate(config: SpecGenerator.Config): Collection<BackupSpec> {
        TODO("not implemented")
    }

    data class Config(
            override val generatorId: UUID, override val label: String
    ) : SpecGenerator.Config {

        override fun getDescription(context: Context): String {
            return generatorId.toString()
        }

        override val generatorType: Backup.Type = Backup.Type.APP
    }
}