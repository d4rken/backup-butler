package eu.darken.bb.backup.core.file

import android.content.Context
import dagger.Reusable
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.CheckSummer
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.file.SFile
import javax.inject.Inject

@Reusable
class FileBackupGenerator @Inject constructor(
        @AppContext private val context: Context
) : Generator {
    override fun generate(config: Generator.Config): Collection<BackupSpec> {
        config as Config
        val specs = mutableListOf<BackupSpec>()

        val app = FileBackupSpec(
                name = CheckSummer.calculate(config.path!!.path, CheckSummer.Type.MD5),
                path = config.path
        )
        specs.add(app)

        return specs

    }

    data class Config(
            override val generatorId: Generator.Id,
            override val label: String = "",
            val path: SFile? = null
    ) : Generator.Config {

        override fun getDescription(context: Context): String {
            return generatorId.toString()
        }

        override val generatorType: Backup.Type = Backup.Type.FILE
    }
}