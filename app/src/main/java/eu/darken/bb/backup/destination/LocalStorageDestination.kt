package eu.darken.bb.backup.destination

import eu.darken.bb.backup.BackupTask
import java.io.File

data class LocalStorageDestination(val path: File) : BackupTask.Destination