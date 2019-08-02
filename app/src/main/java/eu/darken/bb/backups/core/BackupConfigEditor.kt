package eu.darken.bb.backups.core

import io.reactivex.Completable
import io.reactivex.Single
import java.util.*

interface BackupConfigEditor {

    fun load(config: BackupConfig): Completable

    fun save(): Single<BackupConfig>

    interface Factory<EditorT : BackupConfigEditor> {
        fun create(configId: UUID): EditorT
    }

}