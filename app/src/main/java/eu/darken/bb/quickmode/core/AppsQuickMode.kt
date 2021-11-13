package eu.darken.bb.quickmode.core

import com.squareup.moshi.Moshi
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.pkgs.picker.core.PickedPkg
import eu.darken.bb.task.core.TaskRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppsQuickMode @Inject constructor(
    private val taskRepo: TaskRepo,
    private val moshi: Moshi,
    private val settings: QuickModeSettings,
    @AppScope private val appScope: CoroutineScope,
) {

    private val adapter = moshi.adapter(AppsQuickModeConfig::class.java)

    val appsData = DynamicStateFlow(TAG, appScope) {
        settings.rawConfigApps?.let { adapter.fromJson(it) } ?: AppsQuickModeConfig()
    }

    init {
        appsData.flow
            .onEach { settings.rawConfigApps = adapter.toJson(it) }
            .launchIn(appScope)
    }

    suspend fun reset(): QuickMode.Config {
        log(TAG) { "Resetting QuickMode" }
        var oldValue: QuickMode.Config? = null
        appsData.updateBlocking {
            oldValue = this
            AppsQuickModeConfig()
        }
        return oldValue!!
    }

    fun launchBackup(selection: Set<PickedPkg>) {
        TODO()
    }

    companion object {
        private val TAG = logTag("QuickMode", "Repo", "Apps")
    }

}