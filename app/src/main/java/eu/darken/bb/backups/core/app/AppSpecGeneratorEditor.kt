package eu.darken.bb.backups.core.app

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.squareup.moshi.Moshi
import eu.darken.bb.backups.core.SpecGenerator
import eu.darken.bb.common.HotData
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*

class AppSpecGeneratorEditor @AssistedInject constructor(
        moshi: Moshi,
        @Assisted private val generatorId: UUID,
        @Assisted private val parentConfig: SpecGenerator.Config? = null
) : SpecGenerator.Editor {

    override val existingConfig: Boolean = parentConfig != null

    override val allowSave: Boolean
        get() = true

    init {
        if (parentConfig != null && parentConfig !is AppBackupSpecGenerator.Config) {
            throw IllegalArgumentException("$existingConfig is not an ${AppBackupSpecGenerator.Config::class.simpleName}")
        }
    }

    private val configPub = HotData(
            parentConfig as? AppBackupSpecGenerator.Config ?: AppBackupSpecGenerator.Config(
                    generatorId = generatorId,
                    label = ""
            )
    )
    val config: Observable<AppBackupSpecGenerator.Config> = configPub.data.hide()

    fun updateLabel(label: String) {
        configPub.update { it.copy(label = label) }
    }

    override fun save(): Single<SpecGenerator.Config> {
        return configPub.data.firstOrError().map { it }
    }

    @AssistedInject.Factory
    interface Factory : SpecGenerator.Editor.Factory<AppSpecGeneratorEditor>

}