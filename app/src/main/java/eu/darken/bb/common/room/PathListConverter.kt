package eu.darken.bb.common.room

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.task.core.results.TaskResultDatabase
import java.lang.reflect.ParameterizedType

class PathListConverter {

    private val adapter by lazy {
        val type: ParameterizedType = Types.newParameterizedType(List::class.java, APath::class.java)
        val adapter: JsonAdapter<List<APath>> = TaskResultDatabase.moshi.adapter(type)
        adapter
    }

    @TypeConverter
    fun fromValue(value: String?): List<APath>? {
        return value?.let { adapter.fromJson(it) }
    }

    @TypeConverter
    fun toValue(value: List<APath>?): String? {
        return value?.let { adapter.toJson(value) }
    }
}