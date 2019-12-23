package eu.darken.bb.common.room

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import eu.darken.bb.task.core.results.TaskResult
import eu.darken.bb.task.core.results.TaskResultDatabase
import java.lang.reflect.ParameterizedType

class SubResultIdListConverter {

    private val adapter by lazy {
        val type: ParameterizedType = Types.newParameterizedType(List::class.java, TaskResult.SubResult.Id::class.java)
        val adapter: JsonAdapter<List<TaskResult.SubResult.Id>> = TaskResultDatabase.moshi.adapter(type)
        adapter
    }

    @TypeConverter
    fun fromValue(value: String?): List<TaskResult.SubResult.Id>? {
        return value?.let { adapter.fromJson(it) }
    }

    @TypeConverter
    fun toValue(value: List<TaskResult.SubResult.Id>?): String? {
        return value?.let { adapter.toJson(value) }
    }
}