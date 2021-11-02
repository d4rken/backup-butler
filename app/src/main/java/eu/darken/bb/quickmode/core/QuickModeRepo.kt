package eu.darken.bb.quickmode.core

import com.squareup.moshi.Moshi
import eu.darken.bb.common.HotData
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.task.core.TaskRepo
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuickModeRepo @Inject constructor(
    private val taskRepo: TaskRepo,
    private val moshi: Moshi,
    private val settings: QuickModeSettings,
) {

    private val appsConfigAdapter = moshi.adapter(AppsQuickModeConfig::class.java)
    private val filesConfigAdapter = moshi.adapter(FilesQuickModeConfig::class.java)

    val appsData = HotData {
        settings.rawConfigApps?.let { appsConfigAdapter.fromJson(it) } ?: AppsQuickModeConfig()
    }

    val filesData = HotData {
        settings.rawConfigFiles?.let { filesConfigAdapter.fromJson(it) } ?: FilesQuickModeConfig()
    }

    init {
        appsData.data.subscribe {
            settings.rawConfigApps = appsConfigAdapter.toJson(it)
        }
        filesData.data.subscribe {
            settings.rawConfigFiles = filesConfigAdapter.toJson(it)
        }
    }

    fun reset(type: QuickMode.Type): Single<QuickMode.Config> = when (type) {
        QuickMode.Type.APPS -> appsData.updateRx { AppsQuickModeConfig() }
        QuickMode.Type.FILES -> filesData.updateRx { FilesQuickModeConfig() }
    }
        .doOnSubscribe { log(TAG) { "Resetting QuickMode: $type" } }
        .observeOn(Schedulers.computation())
        .map { it.oldValue }


    companion object {
        private val TAG = logTag("QuickMode", "Repo")
    }

}