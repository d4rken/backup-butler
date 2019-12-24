package eu.darken.bb.common.room

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import eu.darken.bb.task.core.results.LogEvent
import eu.darken.bb.task.core.results.TaskResultDatabase
import java.lang.reflect.ParameterizedType


class StringListConverter {

    private val adapter by lazy {
        val type: ParameterizedType = Types.newParameterizedType(List::class.java, LogEvent::class.java)
        val adapter: JsonAdapter<List<String>> = TaskResultDatabase.moshi.adapter(type)
        adapter
    }

    @TypeConverter
    fun fromValue(value: String?): List<String>? = value?.let {
        adapter.fromJson(it)
    }

    @TypeConverter
    fun toValue(strings: List<String>?): String? = strings?.let { adapter.toJson(strings) }
}