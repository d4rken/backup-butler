package eu.darken.bb.backup.processor

import eu.darken.bb.App
import eu.darken.bb.backup.BackupTask
import eu.darken.bb.backup.Destination
import eu.darken.bb.backup.Source
import timber.log.Timber
import javax.inject.Inject

class DefaultBackupProcessor @Inject constructor(

) {
    companion object {
        private val TAG = App.logTag("DefaultBackupProcessor")
    }

    private val sourceProcessors = mapOf<Source.Type, Source.Processor>()
    private val destinationProcessors = mapOf<Destination.Type, Destination.Processor>()

    fun process(backupTask: BackupTask): BackupTask.Result {
        Timber.tag(TAG).i("Processing backup task: %s", backupTask)
        backupTask.sources.forEach { sourceConfig ->
            val sourceProcessor = sourceProcessors[sourceConfig.sourceType]!!
            Timber.tag(TAG).d("Backing up %s using %s", sourceConfig, sourceProcessor)

            val piece = sourceProcessor.backup(sourceConfig)
            Timber.tag(TAG).d("Backup piece created: %s", piece)

            backupTask.destinations.forEach { destinationConfig ->
                val destinationProcessor = destinationProcessors[destinationConfig.destinationType]!!
                Timber.tag(TAG).d("Storing piece %s to %s using %s", piece, destinationConfig, destinationProcessor)

                val result = destinationProcessor.store(piece)
                Timber.tag(TAG).d("Backup piece %s stored: %s", piece, result)
            }
        }

        Thread.sleep(5 * 1000)

        return DefaultBackupTask.Result("123", BackupTask.Result.State.SUCCESS)
    }
}