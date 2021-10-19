package eu.darken.bb.quickmode.core

import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.core.StorageRefRepo
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoSetUp @Inject constructor(
    private val taskBuilder: TaskBuilder,
    private val storageRefRepo: StorageRefRepo,
    private val generatorBuilder: GeneratorBuilder,
    private val storageBuilder: StorageBuilder
) {

    fun setUp(taskId: Task.Id, type: Type): Single<Result> = Single.create<Result> {
        // TODO Load into taskBuilder if not there
        // TODO setup storage if not set
        // TODO split based on Type?
    }.subscribeOn(Schedulers.io())


    data class Result(
        val taskId: Task.Id,
    )

    enum class Type {
        FILES,
        APPS
    }
}