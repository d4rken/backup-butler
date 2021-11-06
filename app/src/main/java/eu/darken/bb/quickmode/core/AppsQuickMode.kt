package eu.darken.bb.quickmode.core

import com.squareup.moshi.Moshi
import eu.darken.bb.common.HotData
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.pkgs.picker.core.PickedPkg
import eu.darken.bb.task.core.TaskRepo
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppsQuickMode @Inject constructor(
    private val taskRepo: TaskRepo,
    private val moshi: Moshi,
    private val settings: QuickModeSettings,
) {

    private val adapter = moshi.adapter(AppsQuickModeConfig::class.java)

    val appsData = HotData {
        settings.rawConfigApps?.let { adapter.fromJson(it) } ?: AppsQuickModeConfig()
    }

    init {
        appsData.data.subscribe {
            settings.rawConfigApps = adapter.toJson(it)
        }
    }

    fun reset(): Single<QuickMode.Config> = appsData
        .updateRx { AppsQuickModeConfig() }
        .doOnSubscribe { log(TAG) { "Resetting QuickMode" } }
        .observeOn(Schedulers.computation())
        .map { it.oldValue }

    fun launchBackup(selection: Set<PickedPkg>) {
        TODO()
    }


    companion object {
        private val TAG = logTag("QuickMode", "Repo", "Apps")
    }

}