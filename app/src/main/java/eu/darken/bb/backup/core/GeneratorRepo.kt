package eu.darken.bb.backup.core

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.backup.core.files.FilesSpecGenerator
import eu.darken.bb.common.collections.mutate
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.debug.logging.Logging.Priority.WARN
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.GatewaySwitch
import eu.darken.bb.common.flow.DynamicStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneratorRepo @Inject constructor(
    @ApplicationContext context: Context,
    moshi: Moshi,
    private val pathTool: GatewaySwitch,
    @AppScope private val scope: CoroutineScope
) {
    private val configAdapter = moshi.adapter(Generator.Config::class.java)
    private val preferences: SharedPreferences = context.getSharedPreferences("backup_generators", Context.MODE_PRIVATE)
    private val state = DynamicStateFlow<Map<Generator.Id, Generator.Config>>(TAG, scope) {
        mutableMapOf<Generator.Id, Generator.Config>().apply {
            preferences.all.forEach {
                val config = configAdapter.fromJson(it.value as String)!!
                this[config.generatorId] = config
            }
        }
    }

    val configs: Flow<Map<Generator.Id, Generator.Config>> = state.flow

    init {
        state.flow
            .onEach { configs ->
                // TODO save in database
                synchronized(this@GeneratorRepo) {
                    preferences.edit().clear().apply()
                    configs.values.forEach {
                        preferences.edit().putString(it.generatorId.toString(), configAdapter.toJson(it)).apply()
                    }
                }
            }
            .launchIn(scope)
    }

    suspend fun get(id: Generator.Id): Generator.Config? = state.value()[id]

    // Puts the spec into storage, returns the previous value
    @Synchronized
    suspend fun put(config: Generator.Config): Generator.Config? {
        var old: Generator.Config? = null
        state.updateBlocking {
            old = this[config.generatorId]
            mutate { this[config.generatorId] = config }
        }
        log(TAG) { "put(config=$config) -> old=$old" }
        return old
    }

    @Synchronized
    suspend fun remove(configId: Generator.Id): Generator.Config? {
        var old: Generator.Config? = null
        state.updateBlocking {
            old = this[configId]
            mutate { this.remove(configId) }
        }
        if (old != null) {
            log(TAG) { "remove(configId=$configId) -> removed=$old" }
        } else {
            log(TAG, WARN) { "remove(configId=$configId) -> Config does not exist!" }
        }
        old?.let {
            if (it is FilesSpecGenerator.Config) {
                pathTool.tryReleaseResources(it.path)
            }
        }
        return old
    }

    companion object {
        val TAG = logTag("Backup", "GeneratorRepo")
    }
}