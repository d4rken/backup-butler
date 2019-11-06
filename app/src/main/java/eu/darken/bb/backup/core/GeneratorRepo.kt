package eu.darken.bb.backup.core

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import eu.darken.bb.App
import eu.darken.bb.backup.core.files.FilesSpecGenerator
import eu.darken.bb.common.Opt
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.file.APathTool
import eu.darken.bb.common.opt
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

@PerApp
class GeneratorRepo @Inject constructor(
        @AppContext context: Context,
        moshi: Moshi,
        private val pathTool: APathTool
) {
    private val configAdapter = moshi.adapter(Generator.Config::class.java)
    private val preferences: SharedPreferences = context.getSharedPreferences("backup_generators", Context.MODE_PRIVATE)
    private val configsPub = BehaviorSubject.create<Map<Generator.Id, Generator.Config>>()
    private val internalConfigs = mutableMapOf<Generator.Id, Generator.Config>()

    val configs: Observable<Map<Generator.Id, Generator.Config>> = configsPub.hide()

    init {
        preferences.all.forEach {
            val config = configAdapter.fromJson(it.value as String)!!
            internalConfigs[config.generatorId] = config
        }
        configsPub.onNext(internalConfigs)
    }

    fun get(id: Generator.Id): Maybe<Generator.Config> = configs.firstOrError()
            .flatMapMaybe { Maybe.fromCallable { it[id] } }

    // Puts the spec into storage, returns the previous value
    @Synchronized fun put(config: Generator.Config): Single<Opt<Generator.Config>> = Single.fromCallable {
        val old = internalConfigs.put(config.generatorId, config)
        Timber.tag(TAG).d("put(spec=%s) -> old=%s", config, old)
        update()
        return@fromCallable old.opt()
    }

    @Synchronized fun remove(configId: Generator.Id): Single<Opt<Generator.Config>> = Single.fromCallable {
        val old = internalConfigs.remove(configId)
        Timber.tag(TAG).d("remove(id=%s) -> old=%s", configId, old)
        if (old is FilesSpecGenerator.Config) {
            pathTool.tryReleaseResources(old.path)
        }
        update()
        if (old == null) Timber.tag(TAG).w("Tried to delete non-existant GeneratorConfig: %s", configId)
        return@fromCallable old.opt()
    }

    @Synchronized private fun update() {
        // TODO save in database
        preferences.edit().clear().apply()
        internalConfigs.values.forEach {
            preferences.edit().putString(it.generatorId.toString(), configAdapter.toJson(it)).apply()
        }
        configsPub.onNext(internalConfigs)
    }

    companion object {
        val TAG = App.logTag("Backup", "GeneratorRepo")
    }
}