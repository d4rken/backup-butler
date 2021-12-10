package eu.darken.bb.backup.core

import kotlinx.coroutines.flow.Flow

interface GeneratorEditor {

    val editorData: Flow<out Data>

    suspend fun load(config: Generator.Config)

    suspend fun save(): Generator.Config

    fun isValid(): Flow<Boolean>

    suspend fun release()

    interface Factory<T : GeneratorEditor> {
        fun create(generatorId: Generator.Id): T
    }

    interface Data {
        val generatorId: Generator.Id
        val label: String
        val isExistingGenerator: Boolean
        val isSingleUse: Boolean
    }
}