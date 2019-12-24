package eu.darken.bb.task.core.results

import android.content.Context
import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.common.IdType
import eu.darken.bb.task.core.Task
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

@Keep
interface TaskResult {
    @Keep
    enum class State constructor(val value: String) {
        SUCCESS("success"), ERROR("error");

        companion object {
            private val VALUE_MAP = values().associateBy(State::value)
            fun fromValue(value: String) = VALUE_MAP[value]
        }
    }

    val resultId: Id
    val taskId: Task.Id
    val taskType: Task.Type
    val label: String
    val startedAt: Date
    val duration: Long
    val state: State
    val primary: String?
    val secondary: String?
    val subResults: List<SubResult>
    val extra: String?

    @Parcelize @Keep
    @JsonClass(generateAdapter = true)
    data class Id(override val value: UUID = UUID.randomUUID()) : IdType<Id>, Parcelable {

        constructor(id: String) : this(UUID.fromString(id))

        @IgnoredOnParcel @Transient override val idString: String = value.toString()

        override fun compareTo(other: Id): Int = value.compareTo(other.value)

        override fun toString(): String = "ResultId($idString)"
    }


    interface Builder<T : TaskResult> {
        fun build(context: Context): T
    }

    @Keep
    interface SubResult {
        val subResultId: Id
        val resultId: TaskResult.Id
        val startedAt: Date
        val duration: Long
        val label: String
        val state: State
        val primary: String?
        val secondary: String?
        val extra: String?
        val logEvents: List<LogEvent>

        // TODO Test

        @Parcelize @Keep
        @JsonClass(generateAdapter = true)
        data class Id(override val value: UUID = UUID.randomUUID()) : IdType<Id>, Parcelable {

            constructor(id: String) : this(UUID.fromString(id))

            @IgnoredOnParcel @Transient override val idString: String = value.toString()

            override fun compareTo(other: Id): Int = value.compareTo(other.value)

            override fun toString(): String = "SubResultId($idString)"
        }

        interface Builder<T : SubResult> {
            fun build(taskResultId: TaskResult.Id): T
        }


    }

}