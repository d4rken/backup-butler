package eu.darken.bb.tasks.core

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.backups.BackupConfig
import eu.darken.bb.common.Jsonable
import eu.darken.bb.storage.core.StorageRef
import java.util.*

interface BackupTask : Jsonable {
    val taskName: String
    val taskId: UUID
    val sources: List<BackupConfig>
    val destinations: List<StorageRef>

    enum class Type {
        SIMPLE
    }

    interface Result {
        enum class State {
            SUCCESS, ERROR
        }

        val taskID: String
        val state: State
        val primary: String?
        val secondary: String?
    }

    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<BackupTask> = PolymorphicJsonAdapterFactory.of(BackupTask::class.java, "taskType")
                .withSubtype(DefaultBackupTask::class.java, Type.SIMPLE.name)
    }
}