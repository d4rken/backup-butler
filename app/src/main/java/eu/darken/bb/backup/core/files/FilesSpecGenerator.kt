package eu.darken.bb.backup.core.files

import android.content.Context
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.files.core.APath
import javax.inject.Inject

@Reusable
class FilesSpecGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) : Generator {
    override suspend fun generate(config: Generator.Config): Collection<BackupSpec> {
        config as Config
        val specs = mutableListOf<BackupSpec>()

        val app = FilesBackupSpec(
            label = config.path.path,
            path = config.path
        )
        specs.add(app)

        return specs
    }

    @Keep
    @JsonClass(generateAdapter = true)
    data class Config(
        override val generatorId: Generator.Id,
        override val label: String,
        val path: APath
    ) : Generator.Config {

        override fun getDescription(context: Context): String {
            return path.path
        }

        override var generatorType: Backup.Type
            get() = Backup.Type.FILES
            set(value) {}
    }
}