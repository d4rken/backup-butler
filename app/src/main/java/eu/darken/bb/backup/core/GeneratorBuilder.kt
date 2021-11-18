package eu.darken.bb.backup.core

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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneratorBuilder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val generatorRepo: GeneratorRepo,
    private val editors: @JvmSuppressWildcards Map<Backup.Type, GeneratorEditor.Factory<out GeneratorEditor>>,
    @AppScope private val appScope: CoroutineScope
) {

    private val dynamicState = DynamicStateFlow<Map<Generator.Id, Data>>(TAG, appScope) { mutableMapOf() }
    val builders = dynamicState.flow

    fun getSupportedBackupTypes(): Flow<Collection<Backup.Type>> = flowOf(Backup.Type.values().toList())

    fun generator(id: Generator.Id): Flow<Data> = dynamicState.flow
        .filter { it.containsKey(id) }
        .map { it[id]!! }

    suspend fun update(id: Generator.Id, action: (Data?) -> Data?): Data? {
        val changedData = dynamicState.updateBlocking {
            val mutMap = this.toMutableMap()
            val old = mutMap.remove(id)

            val new = action.invoke(old)?.let { newData ->
                when {
                    newData.generatorType == null -> newData.copy(editor = null)
                    newData.editor == null -> newData.copy(
                        editor = editors.getValue(newData.generatorType).create(newData.generatorId)
                    )
                    else -> newData
                }
            }

            if (new != null) {
                mutMap[new.generatorId] = new
            }
            mutMap.toMap()
        }
        val updatedData = changedData[id]

        log(TAG, VERBOSE) { "Generator updated: $id ($action): $updatedData" }

        return updatedData
    }

    suspend fun remove(id: Generator.Id, releaseResources: Boolean = true): Data? {
        log(TAG) { "Removing $id" }
        val deleted = dynamicState.value()[id]

        update(id) { null }

        if (releaseResources && deleted != null) {
            deleted.editor?.release()
        }

        log(TAG) { "Removed generator $deleted" }
        return deleted
    }

    suspend fun save(id: Generator.Id): Generator.Config {
        val removed = remove(id, false)
        log(TAG) { "Saving $id" }

        checkNotNull(removed) { "Can't find ID to save: $id" }
        checkNotNull(removed.editor) { "Can't save builder data, NULL editor: $removed" }

        val savedConfig = removed.editor.save()
        generatorRepo.put(savedConfig)

        log { "Saved $id: $savedConfig" }

        return savedConfig
    }

    suspend fun load(id: Generator.Id): Data? {
        log(TAG) { "Loading $id" }
        val config = generatorRepo.get(id)
        if (config == null) {
            log(TAG) { "Couldn't load config for $id, does not exist." }
            return null
        }

        val editor = editors.getValue(config.generatorType).create(config.generatorId)
        editor.load(config)

        val data = Data(
            generatorId = config.generatorId,
            generatorType = config.generatorType,
            editor = editor
        )
        update(id) { data }

        log(TAG) { "Loaded config for $id: $data" }

        return data
    }

    suspend fun getEditor(
        generatorId: Generator.Id = Generator.Id(),
        type: Backup.Type? = null,
    ): Data {
        dynamicState.value()[generatorId]?.let {
            log(TAG) { "getEditor(generatorId=$generatorId, type=$type): Returning cached editor: $it" }
            return it
        }

        load(generatorId)?.let {
            log(TAG) { "getEditor(generatorId=$generatorId, type=$type):  Created editor for existing generator: $it" }
            return it
        }

        log(TAG) { "getEditor(generatorId=$generatorId, type=$type): Creating new generator" }
        val newEditor = Data(
            generatorId = generatorId,
            generatorType = type,
            editor = type?.let { editors.getValue(it).create(generatorId) }
        )

        update(generatorId) { newEditor }
        return newEditor
    }

    data class Data(
        val generatorId: Generator.Id,
        val generatorType: Backup.Type? = null,
        val editor: GeneratorEditor? = null,
    )

    companion object {
        val TAG = logTag("Backup", "Generator", "Builder")
    }
}