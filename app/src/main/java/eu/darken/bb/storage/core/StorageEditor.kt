package eu.darken.bb.storage.core

import eu.darken.bb.common.Opt
import io.reactivex.Single

interface StorageEditor {

    fun load(ref: Storage.Ref): Single<Opt<Storage.Config>>

    fun save(): Single<Pair<Storage.Ref, Storage.Config>>

    fun isExistingStorage(): Boolean

    fun isValidConfig(): Boolean

    interface Factory<EditorT : StorageEditor> {
        fun create(storageId: Storage.Id): EditorT
    }

}