package eu.darken.bb.task.core.results

import android.content.Context
import androidx.room.Room
import eu.darken.bb.App
import eu.darken.bb.Bugs
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.task.core.Task
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@PerApp
class TaskResultRepo @Inject constructor(
        @AppContext private val context: Context
) {

    private val database by lazy {
        Room.databaseBuilder(
                context,
                TaskResultDatabase::class.java, "task_results.db"
        ).build()
    }

    fun submitResult(vararg results: Task.Result) {
        addResult(*results)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {

                        },
                        { cause ->
                            Bugs.track(TAG, ResultRepoException("Failed to submit results: $results", cause))
                        }
                )
    }

    fun addResult(vararg results: Task.Result): Single<List<Long>> = Observable.just(results)
            .flatMapIterable { it.toList() }
            .map {
                StoredResult(
                        resultId = it.resultId,
                        taskId = it.taskId,
                        label = it.label,
                        taskType = it.taskType,
                        startedAt = it.startedAt,
                        duration = it.duration,
                        state = it.state,
                        primary = it.primary,
                        secondary = it.secondary,
                        taskLog = it.taskLog,
                        extra = it.extra
                )
            }
            .toList()
            .flatMap { database.storedResultDao().insertAll(*it.toTypedArray()) }
            .doOnSubscribe { Timber.tag(TAG).v("Saving result: %s", results) }
            .doOnError { Timber.tag(TAG).e(it, "Error saving result: %s", results) }
            .doOnSuccess { Timber.tag(TAG).d("Result saved: %s -> %s", results, it) }

    val results: Flowable<List<Task.Result>> = database.storedResultDao().getAll()
            .doOnSubscribe { Timber.tag(TAG).v("Subscribing to results: %s", it) }
            .doOnError { Timber.tag(TAG).w(it, "Error gathering results.") }
            .doOnNext { Timber.tag(TAG).d("Current results: %s ", it) }
            .doFinally { Timber.tag(TAG).d("FINISHED") }
            as Flowable<List<Task.Result>>

    fun getLatestTaskResults(taskIds: List<Task.Id>) = database.storedResultDao().getLatestTaskResults(taskIds)
            .doOnSubscribe { Timber.tag(TAG).v("Subscribing to for: %s", taskIds) }
            .doOnError { Timber.tag(TAG).w(it, "Error gathering results for %s.", taskIds) }
            .doOnNext { Timber.tag(TAG).d("Current results: %s for %s", it, taskIds) }
            as Observable<List<Task.Result>>


    companion object {
        val TAG = App.logTag("Task", "Result", "Repo")
    }
}