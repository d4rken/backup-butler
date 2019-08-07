package eu.darken.bb.storage.core

import eu.darken.bb.common.Opt
import io.reactivex.Single

interface StorageEditor {

    fun load(ref: StorageRef): Single<Opt<StorageConfig>>

    fun save(): Single<Pair<StorageRef, StorageConfig>>

    fun isExistingStorage(): Boolean

    fun isValidConfig(): Boolean


    interface Factory<EditorT : StorageEditor> {
        fun create(storageId: BackupStorage.Id): EditorT
    }

}