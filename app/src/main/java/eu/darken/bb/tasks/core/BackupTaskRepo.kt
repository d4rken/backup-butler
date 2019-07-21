package eu.darken.bb.tasks.core

import eu.darken.bb.AppComponent
import eu.darken.bb.backup.backups.BackupConfig
import eu.darken.bb.backup.backups.app.AppBackupConfig
import eu.darken.bb.backup.processor.DefaultBackupTask
import eu.darken.bb.backup.repos.BackupRepo
import eu.darken.bb.backup.repos.local.LocalStorageRepoReference
import eu.darken.bb.common.file.JavaFile
import java.util.*
import javax.inject.Inject

@AppComponent.Scope
class BackupTaskRepo @Inject constructor() {
    fun getTask(id: UUID): BackupTask? {
        val appConfig: BackupConfig = AppBackupConfig("eu.thedarken.sdm")
        val backupRepoBackupConfig: BackupRepo.RepoReference = LocalStorageRepoReference(JavaFile.build("/storage/emulated/0/BackupButler/testrepo"))
        val testTask = DefaultBackupTask(
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