package eu.darken.bb.task.core

import android.content.Context
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import eu.darken.bb.R
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.IdType
import eu.darken.bb.common.moshi.MyPolymorphicJsonAdapterFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.task.core.backup.SimpleBackupTask
import eu.darken.bb.task.core.restore.SimpleRestoreTask
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

interface Task {
    val taskId: Id
    val taskType: Type
    val label: String
    val isOneTimeTask: Boolean

    interface Backup : Task {
        val sources: Set<Generator.Id>
        val destinations: Set<Storage.Id>
    }

    interface Restore : Task

    fun getDescription(context: Context): String

    @Keep
    enum class Type(
            @DrawableRes val iconRes: Int,
            @StringRes val labelRes: Int,
            val value: String
    ) {
        BACKUP_SIMPLE(R.drawable.ic_backup, R.string.backup_task_label, "backup_simple"),
        RESTORE_SIMPLE(R.drawable.ic_restore, R.string.restore_task_label, "restore_simple");

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
        val taskType: Type
        val label: String
        val startedAt: Date
        val duration: Long
        val state: State
        val primary: String?
        val secondary: String?
        val extra: String?
        val taskLog: List<String>?

        @Parcelize
        data class Id(override val value: UUID = UUID.randomUUID()) : IdType<Id>, Parcelable {

            constructor(id: String) : this(UUID.fromString(id))

            @IgnoredOnParcel @Transient override val idString: String = value.toString()

            override fun compareTo(other: Id): Int = value.compareTo(other.value)

            override fun toString(): String = "ResultId($idString)"
        }

        interface Builder<T : Task.Result> {
            fun build(context: Context): T
        }
    }

    @Parcelize
    data class Id(override val value: UUID = UUID.randomUUID()) : IdType<Id>, Parcelable {

        constructor(id: String) : this(UUID.fromString(id))

        @IgnoredOnParcel @Transient override val idString = value.toString()

        override fun compareTo(other: Id): Int = value.compareTo(other.value)

        override fun toString(): String = "TaskId($idString)"
    }

    companion object {
        val MOSHI_FACTORY: MyPolymorphicJsonAdapterFactory<Task> = MyPolymorphicJsonAdapterFactory.of(Task::class.java, "taskType")
                .withSubtype(SimpleBackupTask::class.java, Type.BACKUP_SIMPLE.name)
                .withSubtype(SimpleRestoreTask::class.java, Type.RESTORE_SIMPLE.name)
                .skipLabelSerialization()
    }
}