package eu.darken.bb.backups.core.app

import android.content.Context
import dagger.Reusable
import eu.darken.bb.backups.core.Backup
import eu.darken.bb.backups.core.BackupSpec
import eu.darken.bb.backups.core.SpecGenerator
import eu.darken.bb.common.file.SFile
import java.util.*
import javax.inject.Inject

@Reusable
class AppBackupSpecGenerator @Inject constructor() : SpecGenerator {
    override fun generate(config: SpecGenerator.Config): Collection<BackupSpec> {
        config as AppBackupSpecGenerator.Config
        val specs = mutableListOf<BackupSpec>()
        config.packagesIncluded.forEach { pkg ->
            val app = AppBackupSpec(
                    packageName = pkg
            )
            specs.add(app)
        }
        return specs
    }

    data class Config(
            override val generatorId: UUID,
            override val label: String,
            val autoIncludeApps: Boolean = false,
            val includeSystemApps: Boolean = false,
            val packagesIncluded: Collection<String> = listOf(),
            val packagesExcluded: Collection<String> = listOf(),
            val backupApk: Boolean = false,
            val backupData: Boolean = false,
            val extraPaths: Map<String, Collection<SFile>> = emptyMap()
    ) : SpecGenerator.Config {


        override fun getDescription(context: Context): String {
            return generatorId.toString()
        }

        override val generatorType: Backup.Type = Backup.Type.APP
    }
}