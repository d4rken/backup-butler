package eu.darken.bb.backup.core.files

import android.content.Context
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.squareup.moshi.Moshi
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorEditor
import eu.darken.bb.common.HotData
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.file.core.APath
import eu.darken.bb.common.file.core.GatewaySwitch
import eu.darken.bb.common.file.core.saf.SAFGateway
import eu.darken.bb.common.file.core.saf.SAFPath
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class FilesSpecGeneratorEditor @AssistedInject constructor(
        @Assisted private val generatorId: Generator.Id,
        @AppContext private val context: Context,
        moshi: Moshi,
        private val pathTool: GatewaySwitch,
        private val safGateway: SAFGateway
) : GeneratorEditor {

    private val editorDataPub = HotData(Data(generatorId = generatorId))
    override val editorData = editorDataPub.data

    private var originalPath: APath? = null

    override fun load(config: Generator.Config): Completable = Single.just(config as FilesSpecGenerator.Config)
            .flatMap { genSpec ->
                require(generatorId == genSpec.generatorId) { "IDs don't match" }

                originalPath = genSpec.path

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
        val data = editorDataPub.snapshot

        if (data.path != originalPath && originalPath is SAFPath) {
            originalPath?.let { safGateway.releasePermission(it as SAFPath) }
        }

        if (data.path is SAFPath) {
            require(safGateway.takePermission(data.path)) { "We persisted the permission but it's still unavailable?!" }
        }

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

    fun updateLabel(label: String): Completable = editorDataPub
            .updateRx { it.copy(label = label) }
            .ignoreElement()

    fun updatePath(path: APath): Completable = Completable
            .fromCallable { require(pathTool.canRead(path)) { "Can't read $path" } }
            .andThen(editorDataPub.updateRx {
                it.copy(
                        path = path,
                        label = if (it.label == "") path.userReadablePath(context) else it.label
                )
            })
            .ignoreElement()

    data class Data(
            override val generatorId: Generator.Id,
            override val label: String = "",
            override val isExistingGenerator: Boolean = false,
            val path: APath? = null
    ) : GeneratorEditor.Data

    @AssistedInject.Factory
    interface Factory : GeneratorEditor.Factory<FilesSpecGeneratorEditor>

}