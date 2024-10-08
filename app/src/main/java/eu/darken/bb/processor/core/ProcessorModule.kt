package eu.darken.bb.processor.core

import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.hilt.InstallIn
import dagger.multibindings.IntoMap
import eu.darken.bb.backup.core.BackupModule
import eu.darken.bb.processor.core.processors.backup.SimpleBackupProcessor
import eu.darken.bb.processor.core.processors.restore.SimpleRestoreProcessor
import eu.darken.bb.task.core.Task
import kotlinx.coroutines.CoroutineScope

@InstallIn(ProcessorComponent::class)
@Module(includes = [BackupModule::class])
abstract class ProcessorModule {

    @Binds
    @ProcessorScope
    abstract fun processorScope(scope: ProcessorCoroutineScope): CoroutineScope

    @Binds
    @IntoMap
    @TaskType(Task.Type.BACKUP_SIMPLE)
    abstract fun backupSimple(processor: SimpleBackupProcessor.Factory): Processor.Factory<out Processor>

    @Binds
    @IntoMap
    @TaskType(Task.Type.RESTORE_SIMPLE)
    abstract fun restoreSimple(processor: SimpleRestoreProcessor.Factory): Processor.Factory<out Processor>
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class TaskType(val value: Task.Type)