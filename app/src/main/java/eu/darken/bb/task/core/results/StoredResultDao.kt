package eu.darken.bb.task.core.results

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import eu.darken.bb.task.core.Task
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface StoredResultDao {

    @Query("SELECT * FROM task_results WHERE resultId=:id")
    fun get(id: Task.Result.Id): Single<StoredResult>

    @Query("SELECT MAX(startedAt),* FROM task_results WHERE taskId IN (:ids) GROUP BY taskId")
    fun getLatestTaskResults(ids: List<Task.Id>): Observable<List<StoredResult>>

    @Query("SELECT * FROM task_results")
    fun getAll(): Flowable<List<StoredResult>>

    @Insert
    fun insertAll(vararg results: StoredResult): Single<List<Long>>
}