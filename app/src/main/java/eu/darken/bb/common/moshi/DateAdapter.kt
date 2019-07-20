package eu.darken.bb.common.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.*

class DateAdapter {
    @ToJson
    fun toJson(date: Date): Long = date.time

    @FromJson
    fun fromJson(time: Long): Date = Date(time)
}
