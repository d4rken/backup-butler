package eu.darken.bb.backup.core.app

import android.content.Context
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import dagger.Reusable
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.pkgs.NormalPkg
import eu.darken.bb.common.pkgs.Pkg
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import javax.inject.Inject

@Reusable
class AppSpecGenerator @Inject constructor(
        @AppContext private val context: Context,
        private val pkgOps: PkgOps
) : Generator {

    override fun generate(config: Generator.Config): Collection<BackupSpec> {
        config as Config
        val specs = mutableListOf<AppBackupSpec>()

        val allPkgs = pkgOps.listPkgs().filter { it.packageType == Pkg.Type.NORMAL }.map { it as NormalPkg }

        val targetPackages = mutableSetOf<String>()
        if (config.autoInclude) {
            if (config.includeUserApps) {
                val userApps = allPkgs.filterNot { it.isSystemApp }.map { it.packageName }
                targetPackages.addAll(userApps)
            }
            if (config.includeSystemApps) {
                val systemApps = allPkgs.filter { it.isSystemApp }.map { it.packageName }
                targetPackages.addAll(systemApps)
            }
        }

        allPkgs.map { it.packageName }.filter { config.packagesIncluded.contains(it) }.forEach { targetPackages.add(it) }

        targetPackages.removeAll { config.packagesExcluded.contains(it) }

        targetPackages
                .map { pkgName ->
                    AppBackupSpec(
                            packageName = pkgName,
                            backupApk = config.backupApk,
                            backupData = config.backupData,
                            backupCache = config.backupCache,
                            extraPaths = config.extraPaths[pkgName] ?: emptySet()
                    )
                }
                .forEach { specs.add(it) }

        return specs
    }

    @Keep
    @JsonClass(generateAdapter = true)
    data class Config(
            override val generatorId: Generator.Id,
            override val label: String,
            val autoInclude: Boolean,
            val includeUserApps: Boolean,
            val includeSystemApps: Boolean,
            val packagesIncluded: Set<String>,
            val packagesExcluded: Set<String>,
            val backupApk: Boolean,
            val backupData: Boolean,
            val backupCache: Boolean,
            val extraPaths: Map<String, Set<APath>>
    ) : Generator.Config {

        override fun getDescription(context: Context): String {
            // TODO useful generator description
            return generatorId.toString()
        }

        override var generatorType: Backup.Type
            get() = Backup.Type.APP
            set(value) {}
    }
}