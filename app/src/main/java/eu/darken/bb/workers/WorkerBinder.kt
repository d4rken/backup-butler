package eu.darken.bb.workers

import androidx.work.ListenableWorker
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class WorkerKey(val value: KClass<out ListenableWorker>)

@Module
interface WorkerBinder {
    @Binds
    @IntoMap
    @WorkerKey(DefaultBackupWorker::class)
    fun defaultBackupWorker(worker: DefaultBackupWorker.Factory): ChildWorkerFactory
}