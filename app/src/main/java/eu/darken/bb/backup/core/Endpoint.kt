package eu.darken.bb.backup.core

import eu.darken.bb.common.progress.Progress

interface Endpoint {
    fun backup(spec: BackupSpec): Backup

    fun restore(backup: Backup): Boolean

    interface Factory<T : Endpoint> {
        fun create(progressClient: Progress.Client?): T
    }
}