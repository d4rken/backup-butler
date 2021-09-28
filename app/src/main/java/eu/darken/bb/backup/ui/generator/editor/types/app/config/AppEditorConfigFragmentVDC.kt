package eu.darken.bb.backup.ui.generator.editor.types.app.config

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.app.AppSpecGeneratorEditor
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class AppEditorConfigFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val builder: GeneratorBuilder
) : SmartVDC() {

    private val navArgs = handle.navArgs<AppEditorConfigFragmentArgs>()
    private val generatorId = navArgs.value.generatorId

    private val stater = Stater(State())
    val state = stater.liveData

    private val editorObs = builder.generator(generatorId)
        .filter { it.editor != null }
        .map { it.editor as AppSpecGeneratorEditor }

    private val editorDataObs = editorObs.switchMap { it.editorData }

    private val editor: AppSpecGeneratorEditor by lazy { editorObs.blockingFirst() }

    val finishEvent = SingleLiveEvent<Any>()

    init {
        editorDataObs
            .subscribe { editorData ->
                stater.update { state ->
                    state.copy(
                        label = editorData.label,
                        isWorking = false,
                        isExisting = editorData.isExistingGenerator,
                        autoInclude = editorData.autoInclude,
                        includeUserApps = editorData.includeUserApps,
                        includeSystemApps = editorData.includeSystemApps,
                        packagesIncluded = editorData.packagesIncluded,
                        packagesExcluded = editorData.packagesExcluded,
                        backupApk = editorData.backupApk,
                        backupData = editorData.backupData,
                        backupCache = editorData.backupCache,
                        extraPaths = editorData.extraPaths
                    )
                }
            }
            .withScopeVDC(this)

        editorObs
            .flatMap { it.isValid() }
            .subscribe { isValid -> stater.update { it.copy(isValid = isValid) } }
            .withScopeVDC(this)

        editorObs
            .flatMap { it.editorData }
            .subscribe { data ->
                stater.update { it.copy(isExisting = data.isExistingGenerator) }
            }
            .withScopeVDC(this)
    }

    fun updateLabel(label: String) {
        editor.updateLabel(label)
    }

    fun onUpdateAutoInclude(enabled: Boolean) {
        editor.update { it.copy(autoInclude = enabled) }
    }

    fun onUpdateIncludeUser(enabled: Boolean) {
        editor.update { it.copy(includeUserApps = enabled) }
    }

    fun onUpdateIncludeSystem(enabled: Boolean) {
        editor.update { it.copy(includeSystemApps = enabled) }
    }

    fun onUpdateBackupApk(enabled: Boolean) {
        editor.update { it.copy(backupApk = enabled) }
    }

    fun onUpdateBackupData(enabled: Boolean) {
        editor.update { it.copy(backupData = enabled) }
    }

    fun onUpdateBackupCache(enabled: Boolean) {
        editor.update { it.copy(backupCache = enabled) }
    }

    fun saveConfig() {
        builder.save(generatorId)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .doFinally { finishEvent.postValue(Any()) }
            .subscribe()
    }

    data class State(
        val label: String = "",
        val includedPackages: List<String> = emptyList(),
        val isWorking: Boolean = false,
        val isValid: Boolean = false,
        val isExisting: Boolean = false,
        val autoInclude: Boolean = false,
        val includeUserApps: Boolean = false,
        val includeSystemApps: Boolean = false,
        val packagesIncluded: Collection<String> = listOf(),
        val packagesExcluded: Collection<String> = listOf(),
        val backupApk: Boolean = false,
        val backupData: Boolean = false,
        val backupCache: Boolean = false,
        val extraPaths: Map<String, Collection<APath>> = emptyMap()
    )

    companion object {
        val TAG = logTag("Generator", "App", "Editor", "Config", "VDC")
    }
}