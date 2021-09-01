package eu.darken.bb.workers

import android.content.Context
import androidx.work.WorkerParameters
import androidx.work.rxjava3.RxWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.task.core.Task
import io.reactivex.rxjava3.core.Single
import timber.log.Timber

class DefaultBackupWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters
) : RxWorker(context, workerParams) {
    companion object {
        val TAG: String = App.logTag("Worker", "DefaultBackup")
    }

    override fun createWork(): Single<Result> {
        Timber.tag(TAG).i("createWork(): %s", this)

        return Single
            .create<Task> {

            }
            .map {
                Result.success()
            }
    }

    private fun doWork() {
    }

    override fun onStopped() {
        Timber.tag(TAG).i("onStopped(): %s", this)
        super.onStopped()
    }

    @AssistedFactory
    interface Factory : ChildWorkerFactory
}

interface ChildWorkerFactory {
    fun create(context: Context, workerParams: WorkerParameters): DefaultBackupWorker
}