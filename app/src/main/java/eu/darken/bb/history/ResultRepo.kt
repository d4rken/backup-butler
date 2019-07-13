package eu.darken.bb.history

import eu.darken.bb.App
import eu.darken.bb.tasks.core.BackupTask
import timber.log.Timber
import javax.inject.Inject

class ResultRepo @Inject constructor(

) {
    companion object {
        private val TAG = App.logTag("ResultRepo")
    }

    fun store(result: BackupTask.Result) {
        Timber.tag(TAG).d("Storing result: %s", result)
    }
}