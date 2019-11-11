package eu.darken.bb.storage.core

import eu.darken.bb.common.file.core.APath
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface StorageEditor {

    val editorData: Observable<out Data>

    fun load(ref: Storage.Ref): Single<out Storage.Config>

    fun save(): Single<Pair<Storage.Ref, Storage.Config>>

    fun isValid(): Observable<Boolean>

    fun release(): Completable

    interface Factory<EditorT : StorageEditor> {
        fun create(initialStorageId: Storage.Id): EditorT
    }

    interface Data {
        val storageId: Storage.Id
        val label: String
        val existingStorage: Boolean
        val refPath: APath?
    }

}