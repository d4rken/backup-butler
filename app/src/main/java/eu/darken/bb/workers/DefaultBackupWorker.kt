package eu.darken.bb.workers

import android.content.Context
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backup.BackupProcessor
import eu.darken.bb.backup.BackupTask
import eu.darken.bb.backup.BackupTaskRepo
import eu.darken.bb.backup.UnknownTaskException
import io.reactivex.Single
import timber.log.Timber

class DefaultBackupWorker @AssistedInject constructor(
        @Assisted private val context: Context,
        @Assisted private val workerParams: WorkerParameters,
        private val taskRepo: BackupTaskRepo,
        private val processor: BackupProcessor
) : RxWorker(context, workerParams) {
    companion object {
        val TAG: String = App.logTag("Worker", "DefaultBackup")
    }

    override fun createWork(): Single<Result> {
        Timber.tag(TAG).i("createWork(): %s", this)

        return Single
                .create<BackupTask> {


                    val task = taskRepo.getTask(workerParams.id)
                    if (task != null) {
                        it.onSuccess(task)
                    } else {
                        it.tryOnError(UnknownTaskException("Unknown backup task ID: ${workerParams.id}"))
                    }
                }
                .map {
                    processor.process(it)
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

    @AssistedInject.Factory
    interface Factory : ChildWorkerFactory
}

interface ChildWorkerFactory {
    fun create(context: Context, workerParams: WorkerParameters): DefaultBackupWorker
}