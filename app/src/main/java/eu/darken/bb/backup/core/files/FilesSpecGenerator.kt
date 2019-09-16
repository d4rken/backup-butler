package eu.darken.bb.backup.core.files

import android.content.Context
import android.os.Environment
import dagger.Reusable
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.file.AFile
import eu.darken.bb.common.file.asSFile
import java.io.File
import javax.inject.Inject

@Reusable
class FilesSpecGenerator @Inject constructor(
        @AppContext private val context: Context
) : Generator {
    override fun generate(config: Generator.Config): Collection<BackupSpec> {
        config as Config
        val specs = mutableListOf<BackupSpec>()

        val app = FilesBackupSpec(
                label = config.path.path,
                path = config.path
        )
        specs.add(app)

        return specs
    }

    data class Config(
            override val generatorId: Generator.Id,
            override val label: String = "",
            val path: AFile = File(Environment.getExternalStorageDirectory(), "Download").asSFile()
    ) : Generator.Config {

        override fun getDescription(context: Context): String {
            return path.path
        }

        override val generatorType: Backup.Type = Backup.Type.FILES
    }
}