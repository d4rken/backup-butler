package eu.darken.bb.tasks.core

import android.content.Context
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.common.Jsonable
import java.util.*

interface BackupTask : Jsonable {
    val taskId: UUID
    val taskName: String
    val sources: Set<UUID>
    val destinations: Set<UUID>

    fun getDescription(context: Context): String

    enum class Type {
        SIMPLE
    }

    interface Result {
        enum class State {
            SUCCESS, ERROR
        }

        val taskID: UUID
        val state: State
        val error: Exception?
        val primary: String?
        val secondary: String?
    }

    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<BackupTask> = PolymorphicJsonAdapterFactory.of(BackupTask::class.java, "taskType")
                .withSubtype(DefaultBackupTask::class.java, Type.SIMPLE.name)
    }
}