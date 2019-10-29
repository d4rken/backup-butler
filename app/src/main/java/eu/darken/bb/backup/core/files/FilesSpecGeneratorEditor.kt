package eu.darken.bb.backup.core.files

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.squareup.moshi.Moshi
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.ui.generator.editor.types.files.FilesEditorFragmentVDC
import eu.darken.bb.common.HotData
import eu.darken.bb.common.file.APath
import eu.darken.bb.common.file.RawPath
import eu.darken.bb.common.file.SAFGateway
import eu.darken.bb.common.file.SAFPath
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber

class FilesSpecGeneratorEditor @AssistedInject constructor(
        @Assisted private val generatorId: Generator.Id,
        moshi: Moshi,
        private val safGateway: SAFGateway
) : Generator.Editor {

    private val configPub = HotData(FilesSpecGenerator.Config(
            generatorId = generatorId,
            label = "",
            path = RawPath.build("")
    ))
    override val config: Observable<FilesSpecGenerator.Config> = configPub.data

    override var existingConfig: Boolean = false

    override fun isValid(): Observable<Boolean> = config.map {
        it.label.isNotEmpty() && it.path.path.length > 4
    }

    override fun load(config: Generator.Config): Completable = Completable.fromCallable {
        config as FilesSpecGenerator.Config
        existingConfig = true
        configPub.update { config }
        Any()
    }

    override fun save(): Single<out Generator.Config> = configPub.data.firstOrError()
            .doOnSuccess { config ->
                if (config.path is SAFPath) {
                    try {
                        safGateway.takePermission(config.path)
                    } catch (e: Throwable) {
                        Timber.tag(FilesEditorFragmentVDC.TAG).e(e, "Error while persisting permission")
                        try {
                            safGateway.releasePermission(config.path)
                        } catch (e2: Throwable) {
                            Timber.tag(FilesEditorFragmentVDC.TAG).e(e2, "Error while releasing during error...")
                        }
                        throw e
                    }
                }
            }

    fun updateLabel(label: String) {
        configPub.update { it.copy(label = label) }
    }

    fun updatePath(path: APath) {
        configPub.update { it.copy(path = path) }
    }

    @AssistedInject.Factory
    interface Factory : Generator.Editor.Factory<FilesSpecGeneratorEditor>

}