package eu.darken.bb.main.core.simple

import eu.darken.bb.common.HotData
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskRepo
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimpleModeRepo @Inject constructor(
    private val taskRepo: TaskRepo,
) {

    val appsData = HotData { AppsTaskData() }

    data class AppsTaskData(
        val taskId: Task.Id? = null,
    )

    val filesData = HotData { FilesTaskData() }

    data class FilesTaskData(
        val taskId: Task.Id? = null,
    )

    fun removeFilesTask(): Completable = removeTask(Type.FILES)

    private fun removeTask(type: Type): Completable = when (type) {
        Type.APPS -> filesData.updateRx { FilesTaskData() }
        Type.FILES -> filesData.updateRx { FilesTaskData() }
    }
        .doOnSubscribe { log(TAG) { "Removing task $type" } }
        .observeOn(Schedulers.computation())
        .map { it.oldValue }
        .flatMapCompletable {
            log(TAG) { "Task removed: $it" }
            if (it.taskId != null) {
                taskRepo.remove(it.taskId).ignoreElement()
            } else {
                Completable.complete()
            }
        }

    enum class Type {
        APPS,
        FILES
    }

    companion object {
        private val TAG = logTag("SimpleMode", "Repo")
    }

}