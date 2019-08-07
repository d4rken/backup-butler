package eu.darken.bb.task.core

import android.content.Context
import android.os.Parcelable
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.Jsonable
import eu.darken.bb.storage.core.Storage
import kotlinx.android.parcel.Parcelize
import java.util.*

interface Task : Jsonable {
    val taskId: Id
    val taskName: String
    val sources: Set<Generator.Id>
    val destinations: Set<Storage.Id>

    fun getDescription(context: Context): String

    enum class Type {
        SIMPLE
    }

    interface Result {
        enum class State {
            SUCCESS, ERROR
        }

        val taskID: Id
        val state: State
        val error: Exception?
        val primary: String?
        val secondary: String?
    }

    @Parcelize
    data class Id(val id: UUID = UUID.randomUUID()) : Parcelable {
        override fun toString(): String = "TaskId($id)"
    }

    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<Task> = PolymorphicJsonAdapterFactory.of(Task::class.java, "taskType")
                .withSubtype(DefaultTask::class.java, Type.SIMPLE.name)
    }
}