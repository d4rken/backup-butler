package eu.darken.bb.task.core.results

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.results.stored.StoredLogEvent
import eu.darken.bb.task.core.results.stored.StoredResult
import eu.darken.bb.task.core.results.stored.StoredSubResult
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

@Dao
interface StoredResultDao {

    @Query("SELECT * FROM task_results WHERE id = :id")
    fun get(id: TaskResult.Id): Single<StoredResult>

    @Query("SELECT MAX(startedAt),* FROM task_results WHERE taskId IN (:ids) GROUP BY taskId")
    fun getLatestTaskResults(ids: List<Task.Id>): Observable<List<StoredResult>>

    @Query("SELECT * FROM task_results WHERE taskId = :id")
    fun getTaskResult(id: Task.Id): Single<StoredResult>

    @Query("SELECT * FROM task_results WHERE taskId IN (:ids)")
    fun getTaskResults(ids: List<Task.Id>): Single<List<StoredResult>>

    @Query("SELECT * FROM task_subresults WHERE resultId IN (:ids)")
    fun getSubTaskResults(ids: List<TaskResult.Id>): Single<List<StoredSubResult>>

    @Query("SELECT * FROM task_subresults WHERE resultId = :id")
    fun getSubTaskResults(id: TaskResult.Id): Single<List<StoredSubResult>>

    @Query("SELECT * FROM task_log_events WHERE subResultId in (:ids)")
    fun getLogEvents(ids: List<TaskResult.SubResult.Id>): Single<List<StoredLogEvent>>

    @Query("SELECT * FROM task_log_events WHERE subResultId = :id")
    fun getLogEvents(id: TaskResult.SubResult.Id): Single<List<StoredLogEvent>>

    @Query("SELECT * FROM task_results")
    fun getAll(): Flowable<List<StoredResult>>

    @Insert
    fun insertResult(result: StoredResult): Single<Long>

    @Insert
    fun insertSubResults(subResults: List<StoredSubResult>): Single<List<Long>>

    @Insert
    fun insertLogEvents(logEvents: List<StoredLogEvent>): Single<List<Long>>
}