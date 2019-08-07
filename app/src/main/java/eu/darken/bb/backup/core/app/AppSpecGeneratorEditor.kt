package eu.darken.bb.backup.core.app

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.squareup.moshi.Moshi
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.HotData
import io.reactivex.Observable
import io.reactivex.Single

class AppSpecGeneratorEditor @AssistedInject constructor(
        moshi: Moshi,
        @Assisted private val generatorId: Generator.Id,
        @Assisted private val parentConfig: Generator.Config? = null
) : Generator.Editor {

    override val existingConfig: Boolean = parentConfig != null

    override val allowSave: Boolean
        get() = true

    init {
        if (parentConfig != null && parentConfig !is AppBackupGenerator.Config) {
            throw IllegalArgumentException("$existingConfig is not an ${AppBackupGenerator.Config::class.simpleName}")
        }
    }

    private val configPub = HotData(
            parentConfig as? AppBackupGenerator.Config ?: AppBackupGenerator.Config(
                    generatorId = generatorId,
                    label = ""
            )
    )
    val config: Observable<AppBackupGenerator.Config> = configPub.data.hide()

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