package eu.darken.bb.storage.core

import eu.darken.bb.common.Opt
import io.reactivex.Observable
import io.reactivex.Single

interface StorageEditor {

    val config: Observable<out Storage.Config>

    val isExistingStorage: Boolean

    fun isValid(): Observable<Boolean>

    fun load(ref: Storage.Ref): Single<Opt<Storage.Config>>

    fun save(): Single<Pair<Storage.Ref, Storage.Config>>

    interface Factory<EditorT : StorageEditor> {
        fun create(storageId: Storage.Id): EditorT
    }

}