package eu.darken.bb.common.room

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import eu.darken.bb.task.core.results.TaskResultDatabase
import eu.darken.bb.task.core.results.stored.StoredLogEvent
import java.lang.reflect.ParameterizedType

class StoredLogActionListConverter {

    private val adapter by lazy {
        val type: ParameterizedType = Types.newParameterizedType(List::class.java, StoredLogEvent::class.java)
        val adapter: JsonAdapter<List<StoredLogEvent>> = TaskResultDatabase.moshi.adapter(type)
        adapter
    }

    @TypeConverter
    fun fromValue(value: String?): List<StoredLogEvent>? {
        return value?.let { adapter.fromJson(it) }
    }

    @TypeConverter
    fun toValue(value: List<StoredLogEvent>?): String? {
        return value?.let { adapter.toJson(value) }
    }
}