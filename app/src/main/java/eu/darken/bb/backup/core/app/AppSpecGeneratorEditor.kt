package eu.darken.bb.backup.core.app

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorEditor
import eu.darken.bb.common.HotData
import eu.darken.bb.common.file.core.APath
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber

class AppSpecGeneratorEditor @AssistedInject constructor(
        @Assisted private val generatorId: Generator.Id,
        private val appSpecGenerator: AppSpecGenerator
) : GeneratorEditor {

    private val editorDataPub = HotData(Data(generatorId = generatorId))
    override val editorData = editorDataPub.data

    override fun load(config: Generator.Config): Completable = Single.just(config as AppSpecGenerator.Config)
            .flatMap { genSpec ->
                require(generatorId == genSpec.generatorId) { "IDs don't match" }
                editorDataPub.updateRx {
                    it.copy(
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
            .ignoreElement()

    override fun save(): Single<out Generator.Config> = Single.fromCallable {
        val data = editorDataPub.snapshot

        val config = AppSpecGenerator.Config(
                generatorId = data.generatorId,
                label = data.label,
                autoInclude = data.autoInclude,
                includeUserApps = data.includeUserApps,
                includeSystemApps = data.includeSystemApps,
                packagesIncluded = data.packagesIncluded,
                packagesExcluded = data.packagesExcluded,
                backupApk = data.backupApk,
                backupData = data.backupData,
                backupCache = data.backupCache,
                extraPaths = data.extraPaths
        )
        val gens = appSpecGenerator.generate(config)
        Timber.tag(App.logTag("###")).i("AppBackupSpecs: %s", gens)
        return@fromCallable config
    }

    override fun release(): Completable = Completable.complete()

    override fun isValid(): Observable<Boolean> = editorData.map { true }

    fun updateLabel(label: String) {
        editorDataPub.update { it.copy(label = label) }
    }

    fun update(update: (Data) -> Data) {
        editorDataPub.update(update)
    }

    data class Data(
            override val generatorId: Generator.Id,
            override val label: String = "",
            override val isExistingGenerator: Boolean = false,
            val autoInclude: Boolean = true,
            val includeUserApps: Boolean = true,
            val includeSystemApps: Boolean = false,
            val packagesIncluded: Set<String> = setOf(),
            val packagesExcluded: Set<String> = setOf(),
            val backupApk: Boolean = true,
            val backupData: Boolean = true,
            val backupCache: Boolean = false,
            val extraPaths: Map<String, Set<APath>> = emptyMap()
    ) : GeneratorEditor.Data

    @AssistedInject.Factory
    interface Factory : GeneratorEditor.Factory<AppSpecGeneratorEditor>

}