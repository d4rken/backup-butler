package eu.darken.bb.backup.core.files

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorEditor
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.GatewaySwitch
import eu.darken.bb.common.files.core.saf.SAFGateway
import eu.darken.bb.common.files.core.saf.SAFPath
import eu.darken.bb.common.flow.DynamicStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FilesSpecGeneratorEditor @AssistedInject constructor(
    @Assisted val generatorId: Generator.Id,
    @ApplicationContext private val context: Context,
    moshi: Moshi,
    private val pathTool: GatewaySwitch,
    private val safGateway: SAFGateway,
    @AppScope private val appScope: CoroutineScope,
) : GeneratorEditor {

    private val editorDataPub = DynamicStateFlow(TAG, appScope) { Data(generatorId = generatorId) }
    override val editorData = editorDataPub.flow

    private var originalPath: APath? = null

    override suspend fun load(config: Generator.Config) {
        config as FilesSpecGenerator.Config

        require(generatorId == config.generatorId) { "IDs don't match" }

        originalPath = config.path

        editorDataPub.updateBlocking {
            copy(
                label = config.label,
                isExistingGenerator = true,
                path = config.path,
                isSingleUse = isSingleUse
            )
        }
        log(TAG) { "load() successful: $config" }
    }

    override suspend fun save(): Generator.Config {
        val data = editorDataPub.value()

        if (data.path != originalPath && originalPath is SAFPath) {
            originalPath?.let { safGateway.releasePermission(it as SAFPath) }
        }

        if (data.path is SAFPath) {
            require(safGateway.takePermission(data.path)) { "We persisted the permission but it's still unavailable?!" }
        }

        val config = FilesSpecGenerator.Config(
            generatorId = data.generatorId,
            label = data.label,
            path = data.path!!
        )
        log(TAG) { "save()'ed $config" }
        return config
    }

    override suspend fun release() {
        log(TAG) { "release()" }
    }

    override fun isValid(): Flow<Boolean> = editorData.map {
        it.label.isNotEmpty() && it.path != null
    }

    suspend fun updateLabel(label: String) {
        editorDataPub.updateBlocking {
            copy(label = label)
        }
    }

    suspend fun setSingleUse(isSingleUse: Boolean) = editorDataPub.updateBlocking {
        copy(isSingleUse = isSingleUse)
    }

    suspend fun updatePath(path: APath) {
        val canRead = pathTool.canRead(path)
        require(canRead) { "Can't read $path" }

        editorDataPub.updateBlocking {
            copy(
                path = path,
                label = if (this.label == "") path.userReadablePath(context) else this.label
            )
        }
    }

    data class Data(
        override val generatorId: Generator.Id,
        override val label: String = "",
        override val isExistingGenerator: Boolean = false,
        override val isSingleUse: Boolean = false,
        val path: APath? = null
    ) : GeneratorEditor.Data

    @AssistedFactory
    interface Factory : GeneratorEditor.Factory<FilesSpecGeneratorEditor>

    companion object {
        private val TAG = logTag("Backup", "Files", "Generator", "Editor")
    }

}