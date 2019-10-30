package eu.darken.bb.backup.core.files

import android.content.Context
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.squareup.moshi.Moshi
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorEditor
import eu.darken.bb.common.HotData
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.file.APath
import eu.darken.bb.common.file.SAFGateway
import eu.darken.bb.common.file.SAFPath
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class FilesSpecGeneratorEditor @AssistedInject constructor(
        @Assisted private val generatorId: Generator.Id,
        @AppContext private val context: Context,
        moshi: Moshi,
        private val safGateway: SAFGateway
) : GeneratorEditor {

    private val editorDataPub = HotData(Data(generatorId = generatorId))
    override val editorData = editorDataPub.data

    private var newlyAcquiredPerm: SAFPath? = null

    override fun load(config: Generator.Config): Completable = Single.just(config as FilesSpecGenerator.Config)
            .flatMap { genSpec ->
                require(generatorId == genSpec.generatorId) { "IDs don't match" }
                editorDataPub.updateRx {
                    it.copy(
                            label = genSpec.label,
                            isExistingGenerator = true,
                            path = genSpec.path
                    )
                }
            }
            .ignoreElement()

    override fun save(): Single<out Generator.Config> = Single.fromCallable {
        // TODO take permission?
        val data = editorDataPub.snapshot


        FilesSpecGenerator.Config(
                generatorId = data.generatorId,
                label = data.label,
                path = data.path!!
        )
    }

    override fun release(): Completable = Completable.complete()

    override fun isValid(): Observable<Boolean> = editorData.map {
        it.label.isNotEmpty() && it.path != null
    }

    fun updateLabel(label: String) {
        editorDataPub.update { it.copy(label = label) }
    }

    fun updatePath(path: APath) {
        editorDataPub.update {
            it.copy(
                    path = path,
                    label = if (it.label == "") path.userReadablePath(context) else it.label
            )
        }
    }

    data class Data(
            override val generatorId: Generator.Id,
            override val label: String = "",
            override val isExistingGenerator: Boolean = false,
            val path: APath? = null
    ) : GeneratorEditor.Data

    @AssistedInject.Factory
    interface Factory : GeneratorEditor.Factory<FilesSpecGeneratorEditor>

}