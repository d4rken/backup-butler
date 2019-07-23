package eu.darken.bb.tasks.core

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import eu.darken.bb.App
import eu.darken.bb.backup.backups.BackupConfig
import eu.darken.bb.backup.backups.app.AppBackupConfig
import eu.darken.bb.common.Opt
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.file.JavaFile
import eu.darken.bb.common.opt
import eu.darken.bb.repos.core.RepoRef
import eu.darken.bb.repos.core.RepoRefRepo
import eu.darken.bb.repos.core.local.LocalStorageRepoRef
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@PerApp
class BackupTaskRepo @Inject constructor(
        @AppContext context: Context,
        val moshi: Moshi
) {
    private val taskAdapter = moshi.adapter(BackupTask::class.java)
    private val preferences: SharedPreferences = context.getSharedPreferences("backup_tasks", Context.MODE_PRIVATE)
    private val tasksPub = BehaviorSubject.create<Map<UUID, BackupTask>>()
    private val internalTasks = mutableMapOf<UUID, BackupTask>()

    val tasks: Observable<Map<UUID, BackupTask>> = tasksPub.hide()

    init {
        preferences.all.forEach {
            val task = taskAdapter.fromJson(it.value as String)!!
            internalTasks[task.taskId] = task
        }
        tasksPub.onNext(internalTasks)
    }

    fun get(id: UUID): Single<Opt<BackupTask>> = Single.fromCallable {
        val appConfig: BackupConfig = AppBackupConfig("eu.thedarken.sdm")
        val backupRepoBackupConfig: RepoRef = LocalStorageRepoRef(JavaFile.build("/storage/emulated/0/BackupButler/testrepo"))
        val testTask = DefaultBackupTask(
                taskName = "TaskName ${UUID.randomUUID().toString().substring(0, 4)}",
                taskId = id,
                sources = listOf(appConfig),
                destinations = listOf(backupRepoBackupConfig)

        )
        return@fromCallable testTask.opt()
    }

    @Synchronized fun add(task: BackupTask): Single<Opt<BackupTask>> = Single.fromCallable {
        val old = internalTasks.put(task.taskId, task)
        Timber.d("add(task=%s) -> old=%s", task, old)
        update()
        return@fromCallable old.opt()
    }

    @Synchronized fun remove(taskId: UUID): Single<Opt<BackupTask>> = Single.fromCallable {
        val old = internalTasks.remove(taskId)
        Timber.d("remove(taskId=%s) -> old=%s", taskId, old)
        update()
        if (old == null) Timber.tag(RepoRefRepo.TAG).w("Tried to delete non-existant BackupTask: %s", taskId)
        return@fromCallable old.opt()
    }

    @Synchronized private fun update() {
        internalTasks.values.forEach {
            preferences.edit().putString("${it.taskId}", taskAdapter.toJson(it)).apply()
        }
        tasksPub.onNext(internalTasks)
    }

    companion object {
        val TAG = App.logTag("Task", "Repo")
    }
}