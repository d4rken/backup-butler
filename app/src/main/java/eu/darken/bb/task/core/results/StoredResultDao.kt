package eu.darken.bb.task.core.results

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.results.stored.StoredLogEvent
import eu.darken.bb.task.core.results.stored.StoredResult
import eu.darken.bb.task.core.results.stored.StoredSubResult
import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.flow.Flow

@Dao
interface StoredResultDao {

    @Query("SELECT * FROM task_results WHERE id = :id")
    suspend fun get(id: TaskResult.Id): StoredResult

    @Query("SELECT MAX(startedAt),* FROM task_results WHERE taskId IN (:ids) GROUP BY taskId")
    fun getLatestTaskResults(ids: List<Task.Id>): Flow<List<StoredResult>>

    @Query("SELECT * FROM task_results WHERE taskId = :id")
    suspend fun getTaskResult(id: Task.Id): StoredResult

    @Query("SELECT * FROM task_results WHERE taskId IN (:ids)")
    suspend fun getTaskResults(ids: List<Task.Id>): List<StoredResult>

    @Query("SELECT * FROM task_subresults WHERE resultId IN (:ids)")
    suspend fun getSubTaskResults(ids: List<TaskResult.Id>): List<StoredSubResult>

    @Query("SELECT * FROM task_subresults WHERE resultId = :id")
    suspend fun getSubTaskResults(id: TaskResult.Id): List<StoredSubResult>

    @Query("SELECT * FROM task_log_events WHERE subResultId in (:ids)")
    suspend fun getLogEvents(ids: List<TaskResult.SubResult.Id>): List<StoredLogEvent>

    @Query("SELECT * FROM task_log_events WHERE subResultId = :id")
    suspend fun getLogEvents(id: TaskResult.SubResult.Id): List<StoredLogEvent>

    @Query("SELECT * FROM task_results")
    fun getAll(): Flowable<List<StoredResult>>

    @Insert
    suspend fun insertResult(result: StoredResult): Long

    @Insert
    suspend fun insertSubResults(subResults: List<StoredSubResult>): List<Long>

    @Insert
    suspend fun insertLogEvents(logEvents: List<StoredLogEvent>): List<Long>
}