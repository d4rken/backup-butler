package eu.darken.bb.task.core.results

import android.content.Context
import androidx.room.Room
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.App
import eu.darken.bb.Bugs
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.results.stored.StoredLogEvent
import eu.darken.bb.task.core.results.stored.StoredResult
import eu.darken.bb.task.core.results.stored.StoredSubResult
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskResultRepo @Inject constructor(
    @ApplicationContext private val context: Context,
    private val moshi: Moshi
) {

    private val database by lazy {
        Room.databaseBuilder(
            context,
            TaskResultDatabase::class.java, "task_results.db"
        ).build().apply {
            TaskResultDatabase.moshi = moshi
        }
    }

    fun submitResult(result: TaskResult) {
        addResult(result)
            .subscribeOn(Schedulers.io())
            .subscribe(
                {

                },
                { cause ->
                    Bugs.track(TAG, ResultRepoException("Failed to submit results: $result", cause))
                }
            )
    }

    fun addResult(result: TaskResult): Completable = Single
        .fromCallable {
            val storedResult = StoredResult(
                id = result.resultId,
                taskId = result.taskId,
                label = result.label,
                taskType = result.taskType,
                startedAt = result.startedAt,
                duration = result.duration,
                state = result.state,
                primary = result.primary,
                secondary = result.secondary,
                extra = result.extra
            )
            val storedSubs = mutableListOf<StoredSubResult>()
            val storedLogs = mutableListOf<StoredLogEvent>()
            result.subResults.forEach { sub ->
                storedSubs.add(
                    StoredSubResult(
                        id = sub.subResultId,
                        resultId = storedResult.id,
                        startedAt = sub.startedAt,
                        duration = sub.duration,
                        label = sub.label,
                        state = sub.state,
                        primary = sub.primary,
                        secondary = sub.secondary,
                        extra = sub.extra
                    )
                )
                sub.logEvents.forEach { log ->
                    storedLogs.add(
                        StoredLogEvent(
                            subResultId = sub.subResultId,
                            type = log.type.value,
                            description = log.description.get(context)
                        )
                    )
                }
            }
            return@fromCallable Triple(storedResult, storedSubs, storedLogs)
        }
        .flatMap { (result, subResults, logEvents) ->
            Timber.tag(TAG).d("Inserting result.")
            database.storedResultDao().insertResult(result).`as`(RxJavaBridge.toV3Single())
                .flatMap {
                    Timber.tag(TAG).d("Inserting subresults.")
                    database.storedResultDao().insertSubResults(subResults).`as`(RxJavaBridge.toV3Single())
                }
                .flatMap {
                    Timber.tag(TAG).d("Inserting log events.")
                    database.storedResultDao().insertLogEvents(logEvents).`as`(RxJavaBridge.toV3Single())
                }
        }
        .ignoreElement()
        .doOnSubscribe { Timber.tag(TAG).v("Saving result: %s", result) }
        .doOnError { Timber.tag(TAG).e(it, "Error saving result: %s", result) }
        .doOnComplete { Timber.tag(TAG).d("Result saved: %s", result) }

    fun getLatestTaskResultGlimse(taskIds: List<Task.Id>): Observable<List<GlimpsedTaskResult>> =
        database.storedResultDao().getLatestTaskResults(taskIds).`as`(RxJavaBridge.toV3Observable())
            .map { storedResults ->
                storedResults.map {
                    GlimpsedTaskResult(
                        resultId = it.id,
                        taskId = it.taskId,
                        taskType = it.taskType,
                        label = it.label,
                        startedAt = it.startedAt,
                        duration = it.duration,
                        state = it.state,
                        primary = it.primary,
                        secondary = it.secondary,
                        extra = it.extra
                    )
                }
            }
            .doOnSubscribe { Timber.tag(TAG).v("Subscribing to for: %s", taskIds) }
            .doOnError { Timber.tag(TAG).w(it, "Error gathering results for %s.", taskIds) }
            .doOnNext { Timber.tag(TAG).d("Current results: %s for %s", it, taskIds) }
            .onErrorReturn { emptyList() }

    fun getTaskResult(taskId: Task.Id): Single<SimpleResult> =
        database.storedResultDao().getTaskResult(taskId).`as`(RxJavaBridge.toV3Single())
            .flatMap { storedResult ->
                database.storedResultDao().getSubTaskResults(storedResult.id).`as`(RxJavaBridge.toV3Single())
                    .flatMap { storedSubResults ->
                        database.storedResultDao().getLogEvents(storedSubResults.map { it.id })
                            .`as`(RxJavaBridge.toV3Single())
                            .map { storedLogEvents ->
                                val subResults = storedSubResults.map { storedSubResult ->
                                    val logEvents = storedLogEvents.filter { it.subResultId == storedSubResult.id }
                                        .sortedBy { it.id }
                                        .map { LogEvent(LogEvent.Type.fromString(it.type), it.description) }
                                    SimpleResult.SubResult(
                                        subResultId = storedSubResult.id,
                                        resultId = storedSubResult.resultId,
                                        state = storedSubResult.state,
                                        startedAt = storedSubResult.startedAt,
                                        duration = storedSubResult.duration,
                                        primary = storedSubResult.primary,
                                        secondary = storedSubResult.secondary,
                                        extra = storedSubResult.extra,
                                        label = storedSubResult.label,
                                        logEvents = logEvents
                                    )
                                }
                                return@map SimpleResult(
                                    resultId = storedResult.id,
                                    taskId = storedResult.taskId,
                                    taskType = storedResult.taskType,
                                    label = storedResult.label,
                                    startedAt = storedResult.startedAt,
                                    duration = storedResult.duration,
                                    state = storedResult.state,
                                    primary = storedResult.primary,
                                    secondary = storedResult.secondary,
                                    extra = storedResult.extra,
                                    subResults = subResults
                                )
                            }
                    }
            }
            .doOnSubscribe { Timber.tag(TAG).v("Subscribing to for: %s", taskId) }
            .doOnError { Timber.tag(TAG).w(it, "Error gathering results for %s.", taskId) }
            .doOnSuccess { Timber.tag(TAG).d("Current results: %s for %s", it, taskId) }


    companion object {
        val TAG = App.logTag("Task", "Result", "Repo")
    }
}