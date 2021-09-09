package eu.darken.bb.workers

import androidx.work.ListenableWorker
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class WorkerKey(val value: KClass<out ListenableWorker>)

@InstallIn(SingletonComponent::class)
@Module
interface WorkerBinder {
    @Binds
    @IntoMap
    @WorkerKey(DefaultBackupWorker::class)
    fun defaultBackupWorker(worker: DefaultBackupWorker.Factory): ChildWorkerFactory
}