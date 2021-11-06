package eu.darken.bb.processor.core.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.HotData
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.rx.blockingGet2
import eu.darken.bb.processor.core.Processor
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskRepo
import eu.darken.bb.task.core.getTaskId
import eu.darken.bb.task.core.results.TaskResult
import eu.darken.bb.task.core.results.TaskResultRepo
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import java.lang.Thread.sleep
import javax.inject.Inject

@AndroidEntryPoint
class ProcessorService : IntentService(TAG), Progress.Host, Progress.Client, HasContext {

    companion object {
        val TAG = logTag("Processor", "Service")
    }

    init {
        setIntentRedelivery(true)
    }

    @Inject lateinit var notifications: ProcessorNotifications
    @Inject lateinit var processorFactories: @JvmSuppressWildcards Map<Task.Type, Processor.Factory<out Processor>>
    @Inject lateinit var taskRepo: TaskRepo
    @Inject lateinit var serviceControl: ProcessorControl
    @Inject lateinit var resultRepo: TaskResultRepo

    override val context: Context = this

    private val serviceDeathDisp = CompositeDisposable()

    private val progressUpdater = HotData(tag = TAG) { Progress.Data() }
    override val progress: Observable<Progress.Data> = progressUpdater.data

    override fun onCreate() {
        super.onCreate()
        updateProgressPrimary(R.string.progress_preparing_label)
        notifications.start(this)
        serviceControl.updateProgressHost(this)
    }

    override fun onDestroy() {
        progressUpdater.close()
        serviceControl.updateProgressHost(null)
        serviceDeathDisp.dispose()
        notifications.stop(this)
        super.onDestroy()
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) {
            Timber.tag(TAG).e("Null intent!?")
            return
        }
        updateProgressPrimary(R.string.progress_loading_task_label)
        val taskID = intent.getTaskId()
        if (taskID == null) {
            Timber.tag(TAG).e("Intent had no UUID: %s", intent)
            return
        }

        val task = taskRepo.get(taskID).blockingGet2()
        if (task == null) {
            Timber.tag(TAG).e("Unknown backup task: %s", taskID)
            return
        }

        Timber.tag(TAG).i("Processing task: %s", task)

        val processor = processorFactories.getValue(task.taskType).create(this)
        val taskResult = processor.process(task)
        resultRepo.submitResult(taskResult)

        if (task.isOneTimeUse) {
            Timber.tag(TAG).i("Removing one-time-task: %s", task)
            taskRepo.remove(task.taskId).blockingGet()
        }
        if (taskResult.state == TaskResult.State.SUCCESS) {
            Timber.tag(TAG).i("Successfully processed %s: %s", task, taskResult)
        } else {
            Timber.tag(TAG).e("Error while processing %s: %s", task, taskResult)
        }
        sleep(2000)
    }

    override fun updateProgress(update: (Progress.Data) -> Progress.Data) {
        progressUpdater.update(update)
    }

}
