package eu.darken.bb.common.room

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.ParameterizedType


class StringListConverter {
    private val type: ParameterizedType = Types.newParameterizedType(List::class.java, String::class.java)
    private val adapter: JsonAdapter<List<String>> = Moshi.Builder().build().adapter(type)

    @TypeConverter
    fun fromValue(value: String?): List<String>? = value?.let {
        adapter.fromJson(it)
    }

    @TypeConverter
    fun toValue(strings: List<String>?): String? = strings?.let { adapter.toJson(strings) }
}