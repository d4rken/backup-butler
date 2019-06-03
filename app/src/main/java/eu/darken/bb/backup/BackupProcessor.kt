package eu.darken.bb.backup

import dagger.Reusable
import javax.inject.Inject

@Reusable
class BackupProcessor @Inject constructor(
) {

    fun process(backupTask: BackupTask): BackupTask.Result {


        return BackupTaskResult(true)
    }
}