package eu.darken.bb.task.core

import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.task.core.backup.SimpleBackupTaskEditor
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor

@Module
abstract class TaskTypeModule {

    @Binds
    @IntoMap
    @TaskTypeKey(Task.Type.BACKUP_SIMPLE)
    abstract fun backupSimple(repo: SimpleBackupTaskEditor.Factory): TaskEditor.Factory<out TaskEditor>

    @Binds
    @IntoMap
    @TaskTypeKey(Task.Type.RESTORE_SIMPLE)
    abstract fun restoreSimple(repo: SimpleRestoreTaskEditor.Factory): TaskEditor.Factory<out TaskEditor>
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class TaskTypeKey(val value: Task.Type)