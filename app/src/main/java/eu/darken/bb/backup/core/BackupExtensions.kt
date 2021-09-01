package eu.darken.bb.backup.core

import android.content.Intent
import android.os.Bundle
import eu.darken.bb.backup.core.BackupExtensions.BACKUPSPECID_KEY

object BackupExtensions {
    internal const val BACKUPSPECID_KEY = "backupspec.id"
    internal const val BACKUPID_KEY = "backup.id"
}

// BackupSpecs

// Activities

fun Intent.putBackupSpecId(id: BackupSpec.Id) = apply { putExtra(BACKUPSPECID_KEY, id) }
fun Intent.getBackupSpecId(): BackupSpec.Id? = getParcelableExtra(BACKUPSPECID_KEY) as BackupSpec.Id?
fun Intent.putBackupSpecIds(ids: Collection<BackupSpec.Id>) = apply { putExtra(BACKUPSPECID_KEY, ArrayList(ids)) }
fun Intent.getBackupSpecIds(): Collection<BackupSpec.Id>? = getParcelableArrayListExtra(BACKUPSPECID_KEY)

// Fragments

fun Bundle.putBackupSpecId(id: BackupSpec.Id) = apply { putParcelable(BACKUPSPECID_KEY, id) }
fun Bundle.getBackupSpecId(): BackupSpec.Id? = getParcelable(BACKUPSPECID_KEY) as BackupSpec.Id?
fun Bundle.putBackupSpecIds(ids: Collection<BackupSpec.Id>) =
    apply { putParcelableArrayList(BACKUPSPECID_KEY, ArrayList(ids)) }

fun Bundle.getBackupSpecIds(): Collection<BackupSpec.Id>? = getParcelableArrayList(BACKUPSPECID_KEY)


// Backups


fun Intent.putBackupId(id: Backup.Id) = apply { putExtra(BackupExtensions.BACKUPID_KEY, id) }
fun Intent.getBackupId(): Backup.Id? = getParcelableExtra(BackupExtensions.BACKUPID_KEY) as Backup.Id?
fun Intent.putBackupIds(ids: Collection<Backup.Id>) = apply { putExtra(BackupExtensions.BACKUPID_KEY, ArrayList(ids)) }
fun Intent.getBackupIds(): Collection<Backup.Id>? = getParcelableArrayListExtra(BackupExtensions.BACKUPID_KEY)


fun Bundle.putBackupId(id: Backup.Id) = apply { putParcelable(BackupExtensions.BACKUPID_KEY, id) }
fun Bundle.getBackupId(): Backup.Id? = getParcelable(BackupExtensions.BACKUPID_KEY) as Backup.Id?
fun Bundle.putBackupIds(ids: Collection<Backup.Id>) =
    apply { putParcelableArrayList(BackupExtensions.BACKUPID_KEY, ArrayList(ids)) }

fun Bundle.getBackupIds(): Collection<Backup.Id>? = getParcelableArrayList(BackupExtensions.BACKUPID_KEY)