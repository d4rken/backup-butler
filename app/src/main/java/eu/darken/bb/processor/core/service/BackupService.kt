package eu.darken.bb.processor.core.service

import android.app.IntentService
import android.content.Intent
import dagger.android.AndroidInjection
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.common.rx.withCompositeDisposable
import eu.darken.bb.processor.core.DefaultBackupProcessor
import eu.darken.bb.task.core.BackupTaskRepo
import eu.darken.bb.task.core.getTaskId
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class BackupService : IntentService(TAG), ProgressHost, Progressable {

    companion object {
        val TAG = App.logTag("BackupService")
    }

    init {
        setIntentRedelivery(true)
    }

    @Inject lateinit var notifications: BackupNotifications
    @Inject lateinit var backupProcessor: DefaultBackupProcessor
    @Inject lateinit var backupTaskRepo: BackupTaskRepo

    private val progressPub = BehaviorSubject.create<ProgressHost.State>()
    private val serviceDeathDisp = CompositeDisposable()

    override val progress: Observable<ProgressHost.State>
        get() = progressPub

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        progressPub.onNext(ProgressHost.State(getString(R.string.label_progress_preparing)))
        notifications.start(this)
    }

    override fun onDestroy() {
        progressPub.onComplete()
        notifications.stop(this)
        super.onDestroy()
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) {
            Timber.tag(TAG).e("Null intent!?")
            return
        }
        publishPrimary(R.string.label_progress_loading_task)
        val taskID = intent.getTaskId()
        if (taskID == null) {
            Timber.tag(TAG).e("Intent had no UUID: %s", intent)
            return
        }
        val task = backupTaskRepo.get(taskID).blockingGet().value
        if (task == null) {
            Timber.tag(TAG).e("Unknown backup task: %s", taskID)
            return
        }

        Timber.tag(TAG).i("Processing task: %s", task)
        publishPrimary(R.string.label_progress_processing_task)
        val taskResult = backupProcessor.process(task)
        Timber.tag(TAG).i("Finished processing %s: %s", task, taskResult)
    }

    override fun publishPrimary(primary: Int) {
        this.publishPrimary(getString(primary))
    }

    override fun publishPrimary(primary: String) {
        progress.take(1)
                .map { it.copy(primary = primary) }
                .subscribe { progressPub.onNext(it) }
                .withCompositeDisposable(serviceDeathDisp)
    }

    override fun publishSecondary(secondary: Int) {
        this.publishSecondary(getString(secondary))
    }

    override fun publishSecondary(secondary: String) {
        progress.take(1)
                .map { it.copy(secondary = secondary) }
                .subscribe { progressPub.onNext(it) }
                .withCompositeDisposable(serviceDeathDisp)
    }

    override fun publishProgress(current: Long, max: Long, progressType: Progressable.ProgressType) {
        progress.take(1)
                .map { it.copy(progressCurrent = current, progressMax = max, progressType = progressType) }
                .subscribe { progressPub.onNext(it) }
                .withCompositeDisposable(serviceDeathDisp)
    }
}
