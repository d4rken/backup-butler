package eu.darken.bb.task.core.results

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import eu.darken.bb.common.room.*

@Database(entities = [StoredResult::class], version = 1, exportSchema = false)
@TypeConverters(
        TaskResultIdConverter::class,
        TaskIdConverter::class,
        TaskTypeConverter::class,
        TaskResultStateConverter::class,
        DateConverter::class,
        StringListConverter::class
)
abstract class TaskResultDatabase : RoomDatabase() {
    abstract fun storedResultDao(): StoredResultDao
}