package eu.darken.bb.backup.backups

interface BackupEndpoint {
    fun backup(config: BackupConfig): Backup

    fun restore(backup: Backup): Boolean

    interface Factory {
        fun isCompatible(config: BackupConfig): Boolean

        fun create(config: BackupConfig): BackupEndpoint
    }
}