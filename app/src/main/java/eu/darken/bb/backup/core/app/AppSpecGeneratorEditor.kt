package eu.darken.bb.backup.core.app

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.squareup.moshi.Moshi
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.HotData
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class AppSpecGeneratorEditor @AssistedInject constructor(
        @Assisted private val generatorId: Generator.Id,
        moshi: Moshi
) : Generator.Editor {

    private val configPub = HotData(AppSpecGenerator.Config(generatorId = generatorId, label = ""))
    override val config: Observable<AppSpecGenerator.Config> = configPub.data

    override var existingConfig: Boolean = false

    override fun isValid(): Observable<Boolean> = configPub.data.map { true }

    override fun load(config: Generator.Config): Completable = Completable.fromCallable {
        config as AppSpecGenerator.Config
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

    fun updateIncludedPackages(pkgs: List<String>) {
        configPub.update { it.copy(packagesIncluded = pkgs) }
    }

    @AssistedInject.Factory
    interface Factory : Generator.Editor.Factory<AppSpecGeneratorEditor>

}