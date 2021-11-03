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
class FilesQuickMode @Inject constructor(
    private val taskRepo: TaskRepo,
    moshi: Moshi,
    private val settings: QuickModeSettings,
) {

    private val filesConfigAdapter = moshi.adapter(FilesQuickModeConfig::class.java)

    val hotData = HotData {
        settings.rawConfigFiles?.let { filesConfigAdapter.fromJson(it) } ?: FilesQuickModeConfig()
    }

    init {
        hotData.data.subscribe {
            settings.rawConfigFiles = filesConfigAdapter.toJson(it)
        }
    }

    fun reset(type: QuickMode.Type): Single<QuickMode.Config> = hotData
        .updateRx { FilesQuickModeConfig() }
        .doOnSubscribe { log(TAG) { "Resetting QuickMode: $type" } }
        .observeOn(Schedulers.computation())
        .map { it.oldValue }


    companion object {
        private val TAG = logTag("QuickMode", "Repo")
    }

}