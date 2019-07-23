package eu.darken.bb.tasks.core

import eu.darken.bb.App
import eu.darken.bb.common.HotData
import eu.darken.bb.common.dagger.PerApp
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Inject

@PerApp
class TaskBuilder @Inject constructor(
        private val taskRepo: BackupTaskRepo
) {

    private val executor = Executors.newSingleThreadExecutor()
    private val scheduler = Schedulers.from(executor)

    private val hotData = HotData<Map<UUID, BackupTask>>(mutableMapOf())


    fun task(id: UUID, create: (() -> BackupTask)? = null): Observable<BackupTask> {
        var consumed = create == null
        return hotData.data
                .doOnNext {
                    if (!it.containsKey(id) && !consumed) {
                        consumed = true
                        update(id) { create!!.invoke() }
                    }
                }
                .filter { it.containsKey(id) }
                .map { it[id] }
    }

    fun update(taskId: UUID, action: (BackupTask?) -> BackupTask?) {
        hotData.update {
            val mutMap = it.toMutableMap()
            val oldBuilder = mutMap.remove(taskId)
            val newTask = action.invoke(oldBuilder)
            if (newTask != null) {
                mutMap[newTask.taskId] = newTask
            }
            mutMap.toMap()
        }
    }

    fun remove(id: UUID) {
        hotData.update {
            val mutMap = it.toMutableMap()
            mutMap.remove(id)
            mutMap.toMap()
        }
    }

    fun store(id: UUID): Observable<BackupTask> = task(id)
            .observeOn(Schedulers.computation())
            .take(1)
            .flatMapSingle { task ->
                return@flatMapSingle taskRepo.add(task).map { task }
            }
            .doFinally {
                remove(id)
            }

    companion object {
        val TAG = App.logTag("Task", "Builder", "Repo")
    }
}