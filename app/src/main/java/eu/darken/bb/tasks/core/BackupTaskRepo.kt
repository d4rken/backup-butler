package eu.darken.bb.tasks.core

import eu.darken.bb.AppComponent
import eu.darken.bb.backup.backups.BackupConfig
import eu.darken.bb.backup.backups.app.AppBackupConfig
import eu.darken.bb.backup.repos.RepoReference
import eu.darken.bb.backup.repos.local.LocalStorageRepoReference
import eu.darken.bb.common.file.JavaFile
import io.reactivex.Observable
import java.util.*
import javax.inject.Inject

@AppComponent.Scope
class BackupTaskRepo @Inject constructor() {

    fun getTasks(): Observable<Collection<BackupTask>> {
        val tasks = mutableListOf<BackupTask>()
        tasks.add(getTask(UUID.randomUUID())!!)
        tasks.add(getTask(UUID.randomUUID())!!)
        tasks.add(getTask(UUID.randomUUID())!!)
        tasks.add(getTask(UUID.randomUUID())!!)
        tasks.add(getTask(UUID.randomUUID())!!)
        tasks.add(getTask(UUID.randomUUID())!!)
        tasks.add(getTask(UUID.randomUUID())!!)

        return Observable.just(tasks.toList())
    }

    fun getTask(id: UUID): BackupTask? {
        val appConfig: BackupConfig = AppBackupConfig("eu.thedarken.sdm")
        val backupRepoBackupConfig: RepoReference = LocalStorageRepoReference(JavaFile.build("/storage/emulated/0/BackupButler/testrepo"))
        val testTask = DefaultBackupTask(
                taskName = "TaskName ${UUID.randomUUID().toString().substring(0, 4)}",
                id = id,
                sources = listOf(appConfig),
                destinations = listOf(backupRepoBackupConfig)

        )
        return testTask
    }

    fun saveTask(task: BackupTask) {
        TODO("not implemented")
    }

    fun deleteTask(task: BackupTask): BackupTask? {
        TODO("not implemented")
    }
}