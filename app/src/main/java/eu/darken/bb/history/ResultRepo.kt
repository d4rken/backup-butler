package eu.darken.bb.history

import eu.darken.bb.App
import eu.darken.bb.task.core.Task
import timber.log.Timber
import javax.inject.Inject

class ResultRepo @Inject constructor(

) {
    companion object {
        private val TAG = App.logTag("ResultRepo")
    }

    fun store(result: Task.Result) {
        Timber.tag(TAG).d("Storing result: %s", result)
    }
}