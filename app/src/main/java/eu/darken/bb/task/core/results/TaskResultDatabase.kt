package eu.darken.bb.task.core.results

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.squareup.moshi.Moshi
import eu.darken.bb.common.room.*
import eu.darken.bb.task.core.results.stored.StoredResult
import eu.darken.bb.task.core.results.stored.StoredSubResult

@Database(entities = [
    StoredResult::class,
    StoredSubResult::class
], version = 1, exportSchema = false)
@TypeConverters(
        TaskResultIdConverter::class,
        TaskSubResultIdConverter::class,
        SubResultIdListConverter::class,
        TaskIdConverter::class,
        TaskTypeConverter::class,
        StoredLogActionListConverter::class,
        TaskResultStateConverter::class,
        DateConverter::class,
        StringListConverter::class
)
abstract class TaskResultDatabase : RoomDatabase() {

    companion object {
        lateinit var moshi: Moshi
    }

    abstract fun storedResultDao(): StoredResultDao
}