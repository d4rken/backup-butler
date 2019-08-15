package eu.darken.bb.task.core

import android.content.Context
import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.task.core.backup.SimpleBackupTask
import kotlinx.android.parcel.Parcelize
import java.util.*

interface Task {
    val taskId: Id
    val taskName: String
    val taskType: Type

    interface Backup : Task {
        val sources: Set<Generator.Id>
        val destinations: Set<Storage.Id>
    }

    fun getDescription(context: Context): String

    @Keep
    enum class Type(val value: String) {
        BACKUP_SIMPLE("backup_simple");

        companion object {
            private val VALUE_MAP = values().associateBy(Type::value)
            fun fromValue(value: String) = VALUE_MAP[value]
        }
    }

    interface Result {
        @Keep
        enum class State constructor(val value: String) {
            SUCCESS("sucess"), ERROR("success");

            companion object {
                private val VALUE_MAP = values().associateBy(State::value)
                fun fromValue(value: String) = VALUE_MAP[value]
            }
        }

        val resultId: Id
        val taskId: Task.Id
        val taskName: String
        val taskType: Type
        val startedAt: Date
        val duration: Long
        val state: State
        val primary: String?
        val secondary: String?
        val extra: String?

        @Parcelize
        data class Id(val id: UUID = UUID.randomUUID()) : Parcelable {

            constructor(id: String) : this(UUID.fromString(id))

            override fun toString(): String = "ResultId($id)"
        }
    }

    @Parcelize
    data class Id(val id: UUID = UUID.randomUUID()) : Parcelable {

        constructor(id: String) : this(UUID.fromString(id))

        override fun toString(): String = "TaskId($id)"
    }

    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<Task> = PolymorphicJsonAdapterFactory.of(Task::class.java, "taskType")
                .withSubtype(SimpleBackupTask::class.java, Type.BACKUP_SIMPLE.name)
    }
}