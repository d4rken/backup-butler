package eu.darken.bb.task.core.results

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface StoredResultDao {

    @Query("SELECT * FROM task_results")
    fun getAll(): Flowable<List<StoredResult>>

    @Insert
    fun insertAll(vararg results: StoredResult): Single<List<Long>>
}