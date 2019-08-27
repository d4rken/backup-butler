package eu.darken.bb.backup.core.files.legacy

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.squareup.moshi.Moshi
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.HotData
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class LegacyFilesSpecGeneratorEditor @AssistedInject constructor(
        @Assisted private val generatorId: Generator.Id,
        moshi: Moshi
) : Generator.Editor {

    private val configPub = HotData(LegacyFilesSpecGenerator.Config(generatorId = generatorId))
    override val config: Observable<LegacyFilesSpecGenerator.Config> = configPub.data

    override var existingConfig: Boolean = false

    override fun isValid(): Observable<Boolean> = config.map {
        it.label.isNotEmpty() && it.path.path.length > 4
    }

    override fun load(config: Generator.Config): Completable = Completable.fromCallable {
        config as LegacyFilesSpecGenerator.Config
        existingConfig = true
        configPub.update { config }
        Any()
    }

    override fun save(): Single<out Generator.Config> {
        return configPub.data.firstOrError()
    }

    fun updateLabel(label: String) {
        configPub.update { it.copy(label = label) }
    }

    fun updatePath(path: String) {
        TODO()
    }

    @AssistedInject.Factory
    interface Factory : Generator.Editor.Factory<LegacyFilesSpecGeneratorEditor>

}