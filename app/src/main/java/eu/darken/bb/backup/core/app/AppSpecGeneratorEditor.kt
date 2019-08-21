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

    private val configPub = HotData(AppBackupGenerator.Config(generatorId = generatorId, label = ""))

    override var existingConfig: Boolean = false

    override fun isValid(): Observable<Boolean> = configPub.data.map { true }

    override fun load(config: Generator.Config): Completable = Completable.fromCallable {
        existingConfig = true
        Any()
    }

    override val config: Observable<AppBackupGenerator.Config> = configPub.data

    fun updateLabel(label: String) {
        configPub.update { it.copy(label = label) }
    }

    fun updateIncludedPackages(pkgs: List<String>) {
        configPub.update { it.copy(packagesIncluded = pkgs) }
    }

    override fun save(): Single<Generator.Config> {
        return configPub.data.firstOrError().map { it }
    }

    @AssistedInject.Factory
    interface Factory : Generator.Editor.Factory<AppSpecGeneratorEditor>

}