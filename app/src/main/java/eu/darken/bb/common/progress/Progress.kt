package eu.darken.bb.common.progress

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.format.Formatter
import eu.darken.bb.common.AString
import io.reactivex.rxjava3.core.Observable
import kotlin.math.ceil

interface Progress {
    data class Data(
        val icon: Drawable? = null,
        val primary: AString = AString.EMPTY,
        val secondary: AString = AString.EMPTY,
        val tertiary: AString = AString.EMPTY,
        val count: Count = Count.None(),
        val child: Data? = null
    )

    interface Host {
        val progress: Observable<Data>
    }

    interface Client {
        fun updateProgress(update: (Data) -> Data)
    }

    sealed class Count(val current: Long, val max: Long) {
        abstract fun displayValue(context: Context): String?

        class Percent(current: Long, max: Long) : Count(current, max) {

            constructor(current: Int, max: Int) : this(current.toLong(), max.toLong())

            constructor(current: Long) : this(current, 100)

            override fun displayValue(context: Context): String {
                if (current == 0L && max == 0L) return "NaN"
                if (current == 0L) return "0%"
                return "${ceil(((current.toDouble() / max.toDouble()) * 100)).toInt()}%"
            }
        }

        class Size(current: Long, max: Long) : Count(current, max) {
            override fun displayValue(context: Context): String {
                val curSize = Formatter.formatShortFileSize(context, current)
                val maxSize = Formatter.formatShortFileSize(context, max)
                return "$curSize/$maxSize"
            }
        }

        class Counter(current: Long, max: Long) : Count(current, max) {

            constructor(current: Int, max: Int) : this(current.toLong(), max.toLong())

            override fun displayValue(context: Context): String = "$current/$max"
        }

        class Indeterminate : Count(0, 0) {
            override fun displayValue(context: Context): String = ""
        }

        class None : Count(-1, -1) {
            override fun displayValue(context: Context): String? = null
        }
    }
}