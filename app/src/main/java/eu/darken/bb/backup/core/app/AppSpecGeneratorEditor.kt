package eu.darken.bb.backup.core.app

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorEditor
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.flow.DynamicStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

class AppSpecGeneratorEditor @AssistedInject constructor(
    @Assisted private val generatorId: Generator.Id,
    private val appSpecGenerator: AppSpecGenerator,
    @AppScope private val appScope: CoroutineScope,
) : GeneratorEditor {

    private val editorDataPub = DynamicStateFlow(TAG, appScope) { Data(generatorId = generatorId) }
    override val editorData = editorDataPub.flow

    override suspend fun load(config: Generator.Config): Unit {
        val genSpec = config as AppSpecGenerator.Config
        require(generatorId == genSpec.generatorId) { "IDs don't match" }
        editorDataPub.updateBlocking {
            copy(
                generatorId = genSpec.generatorId,
                label = genSpec.label,
                isExistingGenerator = true,
                autoInclude = genSpec.autoInclude,
                includeUserApps = genSpec.includeUserApps,
                includeSystemApps = genSpec.includeSystemApps,
                packagesIncluded = genSpec.packagesIncluded,
                packagesExcluded = genSpec.packagesExcluded,
                backupApk = genSpec.backupApk,
                backupData = genSpec.backupData,
                backupCache = genSpec.backupCache,
                extraPaths = genSpec.extraPaths
            )
        }
    }

    override suspend fun save(): Generator.Config {
        val data = editorDataPub.value()

        val label = if (data.label.isNullOrEmpty()) {
            val sdf = SimpleDateFormat("yyyy.MM.dd hh:mm", Locale.getDefault())
            sdf.format(Date())
        } else {
            data.label
        }

        val config = AppSpecGenerator.Config(
            generatorId = data.generatorId,
            label = label,
            autoInclude = data.autoInclude,
            includeUserApps = data.includeUserApps,
            includeSystemApps = data.includeSystemApps,
            packagesIncluded = data.packagesIncluded,
            packagesExcluded = data.packagesExcluded,
            backupApk = data.backupApk,
            backupData = data.backupData,
            backupCache = data.backupCache,
            extraPaths = data.extraPaths,
            isSingleUse = data.isSingleUse
        )
        val gens = appSpecGenerator.generate(config)
        log(TAG) { "AppBackupSpecs: $gens" }
        return config
    }

    override suspend fun release() {
        log(TAG) { "release()" }
    }

    override fun isValid(): Flow<Boolean> = editorData.map { true }

    suspend fun updateLabel(label: String) = editorDataPub.updateBlocking { copy(label = label) }

    suspend fun update(update: suspend (Data) -> Data) = editorDataPub.updateBlocking(update)


    data class Data(
        override val generatorId: Generator.Id,
        override val label: String = "",
        override val isExistingGenerator: Boolean = false,
        override val isSingleUse: Boolean = false,
        val autoInclude: Boolean = true,
        val includeUserApps: Boolean = true,
        val includeSystemApps: Boolean = false,
        val packagesIncluded: Set<String> = setOf(),
        val packagesExcluded: Set<String> = setOf(),
        val backupApk: Boolean = true,
        val backupData: Boolean = true,
        val backupCache: Boolean = false,
        val extraPaths: Map<String, Set<APath>> = emptyMap(),
    ) : GeneratorEditor.Data

    @AssistedFactory
    interface Factory : GeneratorEditor.Factory<AppSpecGeneratorEditor>


    companion object {
        private val TAG = logTag("Backup", "App", "Generator", "Editor")
    }
}