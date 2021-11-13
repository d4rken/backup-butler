package eu.darken.bb.storage.core

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageBuilder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val refRepo: StorageRefRepo,
    private val editors: @JvmSuppressWildcards Map<Storage.Type, StorageEditor.Factory<out StorageEditor>>,
    @AppScope private val appScope: CoroutineScope
) {

    private val state = DynamicStateFlow<Map<Storage.Id, Data>>(TAG, appScope) { mutableMapOf() }
    val builders = state.flow

    suspend fun getSupportedStorageTypes(): Collection<Storage.Type> {
        return Storage.Type.values().toList()
    }

    fun storage(id: Storage.Id): Flow<Data> = state.flow
        .filter { it.containsKey(id) }
        .map { it.getValue(id) }

    suspend fun update(id: Storage.Id, action: (Data?) -> Data?): Data? {
        val newValue = state.updateBlocking {
            val mutMap = this.toMutableMap()
            val oldStorage = mutMap.remove(id)
            val newStorage = action.invoke(oldStorage)?.let { newData ->
                when {
                    newData.storageType == null -> newData.copy(editor = null)
                    newData.editor == null -> newData.copy(
                        editor = editors.getValue(newData.storageType).create(newData.storageId)
                    )
                    else -> newData
                }
            }
            if (newStorage != null) {
                mutMap[newStorage.storageId] = newStorage
            }
            mutMap.toMap()
        }[id]
        log(TAG, VERBOSE) { "Storage updated: $id ($action): $newValue" }
        return newValue
    }

    suspend fun remove(id: Storage.Id, isAbandon: Boolean = true): Data? {
        log(TAG) { "Removing $id" }

        var removed: Data? = null
        update(id) {
            removed = it
            null
        }

        log(TAG) { "Removed storage: $id -> $removed" }

        if (isAbandon && removed != null) {
            removed?.editor?.abort()
        }
        return removed
    }

    suspend fun save(id: Storage.Id): Storage.Ref {
        log(TAG) { "Saving $id" }

        val removed = remove(id, false)
        checkNotNull(removed) { "Can't find ID to save: $id" }
        checkNotNull(removed.editor) { "Can't save builder data NULL editor: $removed" }

        val (ref, config) = removed.editor.save()

        refRepo.put(ref)
        log(TAG) { "Saved $id: $ref" }

        return ref
    }

    suspend fun load(id: Storage.Id): Data? {
        val ref = refRepo.get(id) ?: return null
        val editor = editors.getValue(ref.storageType).create(ref.storageId)
        editor.load(ref)
        val builderData = Data(
            storageId = ref.storageId,
            storageType = ref.storageType,
            editor = editor
        )
        update(id) { builderData }

        log(TAG) { "Loaded $id: $builderData" }

        return builderData
    }

    suspend fun getEditor(
        storageId: Storage.Id = Storage.Id(),
        type: Storage.Type? = null,
    ): Data {
        state.value()[storageId]?.let {
            log(TAG) { "getEditor(storageId=$storageId, type=$type): Returning cached editor: $it" }
            return it
        }

        load(storageId)?.let {
            log(TAG) { "getEditor(storageId=$storageId, type=$type):  Created editor for existing storage: $it" }
            return it
        }

        log(TAG) { "getEditor(storageId=$storageId, type=$type): Creating new storage editor" }
        val newEditor = StorageBuilder.Data(
            storageId = storageId,
            storageType = type,
            editor = type?.let { editors.getValue(it).create(storageId) }
        )

        update(storageId) { newEditor }

        return newEditor
    }

    data class Data(
        val storageId: Storage.Id,
        val storageType: Storage.Type? = null,
        val editor: StorageEditor? = null
    )

    companion object {
        val TAG = logTag("Storage", "Builder")
    }
}
