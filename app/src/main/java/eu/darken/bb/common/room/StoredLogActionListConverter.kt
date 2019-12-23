package eu.darken.bb.common.room

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import eu.darken.bb.task.core.results.TaskResultDatabase
import eu.darken.bb.task.core.results.stored.StoredIOEvent
import java.lang.reflect.ParameterizedType

class StoredLogActionListConverter {

    private val adapter by lazy {
        val type: ParameterizedType = Types.newParameterizedType(List::class.java, StoredIOEvent::class.java)
        val adapter: JsonAdapter<List<StoredIOEvent>> = TaskResultDatabase.moshi.adapter(type)
        adapter
    }

    @TypeConverter
    fun fromValue(value: String?): List<StoredIOEvent>? {
        return value?.let { adapter.fromJson(it) }
    }

    @TypeConverter
    fun toValue(value: List<StoredIOEvent>?): String? {
        return value?.let { adapter.toJson(value) }
    }
}