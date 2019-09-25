package eu.darken.bb.backup.core.app

import android.content.Context
import dagger.Reusable
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.file.APath
import javax.inject.Inject

@Reusable
class AppSpecGenerator @Inject constructor(
        @AppContext private val context: Context
) : Generator {

    override fun generate(config: Generator.Config): Collection<BackupSpec> {
        config as AppSpecGenerator.Config
        val specs = mutableListOf<BackupSpec>()
        context.packageManager.getInstalledPackages(0)
        // FIXME remove limiter
        context.packageManager.getInstalledPackages(0).subList(0, 10).map { it.packageName }.forEach { pkg ->
            val app = AppBackupSpec(
                    packageName = pkg
            )
            specs.add(app)
        }
//        config.packagesIncluded.forEach { pkg ->
//            val app = AppBackupSpec(
//                    packageName = pkg
//            )
//            specs.add(app)
//        }
        return specs
    }

    data class Config(
            override val generatorId: Generator.Id,
            override val label: String,
            val autoIncludeApps: Boolean = false,
            val includeSystemApps: Boolean = false,
            val packagesIncluded: Collection<String> = listOf(),
            val packagesExcluded: Collection<String> = listOf(),
            val backupApk: Boolean = false,
            val backupData: Boolean = false,
            val extraPaths: Map<String, Collection<APath>> = emptyMap()
    ) : Generator.Config {


        override fun getDescription(context: Context): String {
            return generatorId.toString()
        }

        override val generatorType: Backup.Type = Backup.Type.APP
    }
}