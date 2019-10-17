package eu.darken.bb.storage.core

import eu.darken.bb.common.file.APath
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface StorageEditor {

    val editorData: Observable<out Data>

//    val config: Observable<out Storage.Config>
//
//    val isExistingStorage: Boolean

    fun isValid(): Observable<Boolean>

    fun load(ref: Storage.Ref): Single<out Storage.Config>

    fun save(): Single<Pair<Storage.Ref, Storage.Config>>

    fun release(): Completable

    interface Factory<EditorT : StorageEditor> {
        fun create(storageId: Storage.Id): EditorT
    }

    interface Data {
        val storageId: Storage.Id
        val label: String
        val existingStorage: Boolean
        val refPath: APath?
    }

}