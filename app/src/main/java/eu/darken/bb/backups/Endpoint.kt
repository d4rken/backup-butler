package eu.darken.bb.backups

interface Endpoint {
    fun backup(config: BackupConfig): Backup

    fun restore(backup: Backup): Boolean

    interface Factory {
        fun isCompatible(config: BackupConfig): Boolean

        fun create(config: BackupConfig): Endpoint
    }
}