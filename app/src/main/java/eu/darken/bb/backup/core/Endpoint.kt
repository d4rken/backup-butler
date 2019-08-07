package eu.darken.bb.backup.core

interface Endpoint {
    fun backup(spec: BackupSpec): Backup

    fun restore(backup: Backup): Boolean

    interface Factory {
        fun create(spec: BackupSpec): Endpoint
    }
}