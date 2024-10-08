package eu.darken.bb.task.core

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.squareup.moshi.JsonClass
import eu.darken.bb.R
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.CaString
import eu.darken.bb.common.IdType
import eu.darken.bb.common.moshi.MyPolymorphicJsonAdapterFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.task.core.backup.SimpleBackupTask
import eu.darken.bb.task.core.restore.SimpleRestoreTask
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*

@Keep
interface Task {
    val taskId: Id
    val taskType: Type
    val label: String
    val isSingleUse: Boolean

    @Keep
    interface Backup : Task {
        val sources: Set<Generator.Id>
        val destinations: Set<Storage.Id>
    }

    @Keep
    interface Restore : Task

    fun getDescription(): CaString

    @Parcelize @Keep
    enum class Type(
        @DrawableRes val iconRes: Int,
        @StringRes val labelRes: Int,
        val value: String
    ) : Parcelable {
        BACKUP_SIMPLE(R.drawable.ic_backup, R.string.task_backup_label, "backup_simple"),
        RESTORE_SIMPLE(R.drawable.ic_restore, R.string.task_restore_label, "restore_simple");

        companion object {
            internal val VALUE_MAP = values().associateBy(Type::value)
            fun fromValue(value: String) = VALUE_MAP[value]
        }
    }

    @Parcelize @Keep
    @JsonClass(generateAdapter = true)
    data class Id(override val value: UUID = UUID.randomUUID()) : IdType<Id>, Parcelable {

        constructor(id: String) : this(UUID.fromString(id))

        @IgnoredOnParcel @Transient override val idString = value.toString()

        override fun compareTo(other: Id): Int = value.compareTo(other.value)

        override fun toString(): String = "TaskId($idString)"
    }

    companion object {
        val MOSHI_FACTORY: MyPolymorphicJsonAdapterFactory<Task> =
            MyPolymorphicJsonAdapterFactory.of(Task::class.java, "taskType")
                .withSubtype(SimpleBackupTask::class.java, Type.BACKUP_SIMPLE.name)
                .withSubtype(SimpleRestoreTask::class.java, Type.RESTORE_SIMPLE.name)
                .skipLabelSerialization()
    }
}