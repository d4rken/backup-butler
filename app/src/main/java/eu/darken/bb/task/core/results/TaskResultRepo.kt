package eu.darken.bb.task.core.results

import android.content.Context
import androidx.room.Room
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.Bugs
import eu.darken.bb.common.debug.BBDebug
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.results.stored.StoredLogEvent
import eu.darken.bb.task.core.results.stored.StoredResult
import eu.darken.bb.task.core.results.stored.StoredSubResult
import kotlinx.coroutines.flow.*
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

    suspend fun submitResult(result: TaskResult) = try {
        log(TAG) { "Submitting result: $result" }
        addResult(result)
    } catch (e: Exception) {
        if (BBDebug.isDebug()) {
            throw e
        } else {
            Bugs.track(TAG, ResultRepoException("Failed to submit results: $result", e))
        }
    }

    suspend fun addResult(result: TaskResult) {
        Timber.tag(TAG).v("Saving result: %s", result)
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

        Timber.tag(TAG).d("Inserting result.")
        database.storedResultDao().insertResult(storedResult)

        log(TAG) { "Inserting subresults." }
        database.storedResultDao().insertSubResults(storedSubs)

        log(TAG) { "Inserting log events." }
        database.storedResultDao().insertLogEvents(storedLogs)

        log(TAG) { "Result saved: $result" }
    }

    fun getLatestTaskResultGlimse(taskIds: List<Task.Id>): Flow<List<GlimpsedTaskResult>> =
        database.storedResultDao().getLatestTaskResults(taskIds)
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
            .onStart { Timber.tag(TAG).v("Subscribing to for: %s", taskIds) }
            .catch {
                Timber.tag(TAG).w(it, "Error gathering results for %s.", taskIds)
                if (BBDebug.isDebug()) {
                    throw it
                } else {
                    emit(emptyList())
                }
            }
            .onEach { Timber.tag(TAG).d("Current results: %s for %s", it, taskIds) }


    suspend fun getTaskResult(taskId: Task.Id): SimpleResult {
        Timber.tag(TAG).v("Subscribing to for: %s", taskId)
        val storedResult = database.storedResultDao().getTaskResult(taskId)

        val storedSubResults = database.storedResultDao().getSubTaskResults(storedResult.id)
        val storedLogEvents = database.storedResultDao().getLogEvents(storedSubResults.map { it.id })

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

        val result = SimpleResult(
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

        log(TAG) { "Current results for $taskId: $result" }
        return result
    }

    companion object {
        val TAG = logTag("Task", "Result", "Repo")
    }
}