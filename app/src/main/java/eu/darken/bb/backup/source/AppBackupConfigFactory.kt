package eu.darken.bb.backup.source

import android.content.Context
import eu.darken.bb.backup.Source
import eu.darken.bb.common.dagger.AppContext
import javax.inject.Inject

class AppBackupConfigFactory @Inject constructor(
        @AppContext context: Context
) {

    fun createConfig(packages: List<String>): Source.Config {
        return AppSourceConfig(packages)
    }
}

data class AppSourceConfig(private val packages: List<String>) : Source.Config {
    override val sourceType: Source.Type = Source.Type.APP_BACKUP
}