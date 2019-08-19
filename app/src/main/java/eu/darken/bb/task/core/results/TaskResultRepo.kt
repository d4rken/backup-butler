package eu.darken.bb.task.core.results

import android.content.Context
import androidx.room.Room
import eu.darken.bb.App
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.debug.BugTrack
import eu.darken.bb.task.core.Task
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

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
                            BugTrack.notify(TAG, ResultRepoException("Failed to submit results: $results", cause))
                        }
                )
    }

    fun addResult(vararg results: Task.Result): Single<List<Long>> = Observable.just(results)
            .flatMapIterable { it.toList() }
            .map {
                StoredResult(
                        resultId = it.resultId,
                        taskId = it.taskId,
                        taskName = it.taskName,
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
            as Flowable<List<Task.Result>>


    companion object {
        val TAG = App.logTag("Task", "Result", "Repo")
    }
}