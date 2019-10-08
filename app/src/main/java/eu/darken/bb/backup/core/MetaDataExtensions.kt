package eu.darken.bb.backup.core

fun Collection<Backup.MetaData>.getNewest(): Backup.MetaData? {
    return this.sortedBy { it.createdAt }.reversed().firstOrNull()
}

fun Collection<Backup.MetaData>.getBackup(backupId: Backup.Id): Backup.MetaData? {
    return this.find { it.backupId == backupId }
}