package eu.darken.bb.backup.source.app

import android.content.Context
import eu.darken.bb.backup.Source
import eu.darken.bb.common.dagger.AppContext
import javax.inject.Inject

class AppSourceConfigFactory @Inject constructor(
        @AppContext context: Context
) {

    fun createConfig(packages: List<String>): Source.Config {
        return AppSourceConfig(packages)
    }
}

