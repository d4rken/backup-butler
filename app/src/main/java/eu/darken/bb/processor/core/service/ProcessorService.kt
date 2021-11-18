package eu.darken.bb.processor.core.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.processor.core.Processor
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.processor.core.ProcessorScope
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskRepo
import eu.darken.bb.task.core.getTaskId
import eu.darken.bb.task.core.results.TaskResult
import eu.darken.bb.task.core.results.TaskResultRepo
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
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

    private val launchLock = Mutex()
    @ProcessorScope @Inject lateinit var processorScope: CoroutineScope
    @Inject lateinit var notifications: ProcessorNotifications
    @Inject lateinit var processorFactories: @JvmSuppressWildcards Map<Task.Type, Processor.Factory<out Processor>>
    @Inject lateinit var taskRepo: TaskRepo
    @Inject lateinit var serviceControl: ProcessorControl
    @Inject lateinit var resultRepo: TaskResultRepo
    @ProcessorScope @Inject lateinit var coroutineScope: CoroutineScope

    override val context: Context = this

    private val serviceDeathDisp = CompositeDisposable()

    private val progressUpdater = DynamicStateFlow(TAG, processorScope) { Progress.Data() }
    override val progress: Flow<Progress.Data> = progressUpdater.flow

    override fun onCreate() {
        super.onCreate()
        updateProgressPrimary(R.string.progress_preparing_label)
        notifications.start(this)
        serviceControl.updateProgressHost(this)
    }

    override fun onDestroy() {
        processorScope.cancel()
        serviceControl.updateProgressHost(null)
        serviceDeathDisp.dispose()
        notifications.stop(this)
        super.onDestroy()
    }

    override fun onHandleIntent(intent: Intent?) = runBlocking {
        if (intent == null) {
            Timber.tag(TAG).e("Null intent!?")
            return@runBlocking
        }
        updateProgressPrimary(R.string.progress_loading_task_label)
        val taskID = intent.getTaskId()
        if (taskID == null) {
            Timber.tag(TAG).e("Intent had no UUID: %s", intent)
            return@runBlocking
        }

        val task = taskRepo.get(taskID)
        if (task == null) {
            Timber.tag(TAG).e("Unknown backup task: %s", taskID)
            return@runBlocking
        }

        Timber.tag(TAG).i("Processing task: %s", task)

        val processor = processorFactories.getValue(task.taskType).create(this@ProcessorService)
        val taskResult = processor.process(task)
        resultRepo.submitResult(taskResult)

        if (task.isOneTimeUse) {
            Timber.tag(TAG).i("Removing one-time-task: %s", task)
            taskRepo.remove(task.taskId)
        }
        if (taskResult.state == TaskResult.State.SUCCESS) {
            Timber.tag(TAG).i("Successfully processed %s: %s", task, taskResult)
        } else {
            Timber.tag(TAG).e("Error while processing %s: %s", task, taskResult)
        }
        sleep(2000)
    }

    override fun updateProgress(update: suspend (Progress.Data) -> Progress.Data) {
        progressUpdater.updateAsync(onUpdate = update)
    }

}
