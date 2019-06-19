package eu.darken.bb.backup.repo

import eu.darken.bb.AppComponent
import eu.darken.bb.backup.BackupTask
import java.util.*
import javax.inject.Inject

@AppComponent.Scope
class BackupTaskRepo @Inject constructor() {
    fun getTask(id: UUID): BackupTask? {
        TODO("not implemented")
    }

    fun saveTask(task: BackupTask) {
        TODO("not implemented")
    }

    fun deleteTask(task: BackupTask): BackupTask? {
        TODO("not implemented")
    }
}