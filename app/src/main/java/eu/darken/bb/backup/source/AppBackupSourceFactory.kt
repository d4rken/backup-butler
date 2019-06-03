package eu.darken.bb.backup.source

import android.content.Context
import eu.darken.bb.backup.BackupTask
import eu.darken.bb.common.dagger.ApplicationContext
import javax.inject.Inject

class AppBackupSourceFactory @Inject constructor(
        @ApplicationContext context: Context
) {

    fun createTask(packages: List<String>): BackupTask.Source {
        return AppSource(packages)
    }
}

data class AppSource(private val packages: List<String>) : BackupTask.Source {

    override fun provide(): Pair<BackupTask.Source.MetaData, List<BackupTask.Source.SFile>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}