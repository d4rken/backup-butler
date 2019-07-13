package eu.darken.bb.tasks.core

import eu.darken.bb.AppComponent
import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.backups.app.AppBackup
import eu.darken.bb.backup.processor.DefaultBackupTask
import eu.darken.bb.backup.repos.BackupRepo
import eu.darken.bb.backup.repos.local.LocalStorageRepo
import eu.darken.bb.common.file.JavaFile
import java.util.*
import javax.inject.Inject

@AppComponent.Scope
class BackupTaskRepo @Inject constructor() {
    fun getTask(id: UUID): BackupTask? {
        val appConfig: Backup.Config = AppBackup.Config("org.swiftapps.swiftbackup")
        val backupRepoConfig: BackupRepo.Config = LocalStorageRepo.Config(JavaFile.build("/storage/emulated/0/BackupButler"))
        val testTask = DefaultBackupTask(
                id = id,
                sources = listOf(appConfig),
                destinations = listOf(backupRepoConfig)

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