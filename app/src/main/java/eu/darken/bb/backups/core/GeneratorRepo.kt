package eu.darken.bb.backups.core

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import eu.darken.bb.App
import eu.darken.bb.common.Opt
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.opt
import eu.darken.bb.storage.core.StorageRefRepo
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@PerApp
class GeneratorRepo @Inject constructor(
        @AppContext context: Context,
        moshi: Moshi
) {
    private val configAdapter = moshi.adapter(SpecGenerator.Config::class.java)
    private val preferences: SharedPreferences = context.getSharedPreferences("backup_backupconfig_generators", Context.MODE_PRIVATE)
    private val configsPub = BehaviorSubject.create<Map<UUID, SpecGenerator.Config>>()
    private val internalConfigs = mutableMapOf<UUID, SpecGenerator.Config>()

    val configs: Observable<Map<UUID, SpecGenerator.Config>> = configsPub.hide()

    init {
        preferences.all.forEach {
            val config = configAdapter.fromJson(it.value as String)!!
            internalConfigs[config.generatorId] = config
        }
        configsPub.onNext(internalConfigs)
    }

    fun get(id: UUID): Single<Opt<SpecGenerator.Config>> = configs
            .firstOrError()
            .map { Opt(it[id]) }

    // Puts the spec into storage, returns the previous value
    @Synchronized fun put(config: SpecGenerator.Config): Single<Opt<SpecGenerator.Config>> = Single.fromCallable {
        val old = internalConfigs.put(config.generatorId, config)
        Timber.tag(TAG).d("put(spec=%s) -> old=%s", config, old)
        update()
        return@fromCallable old.opt()
    }

    @Synchronized fun remove(configId: UUID): Single<Opt<SpecGenerator.Config>> = Single.fromCallable {
        val old = internalConfigs.remove(configId)
        Timber.tag(TAG).d("remove(id=%s) -> old=%s", configId, old)
        update()
        if (old == null) Timber.tag(StorageRefRepo.TAG).w("Tried to delete non-existant GeneratorConfig: %s", configId)
        return@fromCallable old.opt()
    }

    @Synchronized private fun update() {
        preferences.edit().clear().apply()
        internalConfigs.values.forEach {
            preferences.edit().putString(it.generatorId.toString(), configAdapter.toJson(it)).apply()
        }
        configsPub.onNext(internalConfigs)
    }

    companion object {
        val TAG = App.logTag("Backup", "ConfigRepo")
    }
}