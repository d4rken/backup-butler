package eu.darken.bb.storage.core

import eu.darken.bb.common.files.core.APath
import kotlinx.coroutines.flow.Flow

interface StorageEditor {

    val editorData: Flow<out Data>

    suspend fun load(ref: Storage.Ref): Storage.Config

    suspend fun save(): Pair<Storage.Ref, Storage.Config>

    fun isValid(): Flow<Boolean>

    suspend fun abort()

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