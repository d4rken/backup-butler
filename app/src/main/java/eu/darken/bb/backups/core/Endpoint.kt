package eu.darken.bb.backups.core

interface Endpoint {
    fun backup(spec: BackupSpec): Backup

    fun restore(backup: Backup): Boolean

    interface Factory {
        fun create(spec: BackupSpec): Endpoint
    }
}