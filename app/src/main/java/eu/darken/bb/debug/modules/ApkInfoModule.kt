package eu.darken.bb.debug

import android.util.Log
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.thedarken.sdm.tools.debug.DebugModuleHost
import io.reactivex.schedulers.Schedulers

class ApkInfoModule @AssistedInject constructor(
        @Assisted val host: DebugModuleHost
) : DebugModule {
    companion object {
        internal val TAG = App.logTag("Debug", "ApkInfo")
    }

    private var previousOptions: DebugOptions = DebugOptions.default()

    init {
        host.observeOptions()
                .observeOn(Schedulers.io())
                .filter { !previousOptions.compareIgnorePath(it) && it.level <= Log.INFO }
                .doOnNext { previousOptions = it }
                .subscribe {
                    //                    val sdmaidInfo = SDMaid.getPackageInfo(host.getContext())
//                    Timber.tag(TAG).i("F: VERSIONNAME: ${sdmaidInfo.versionName}, VERSIONCODE: ${sdmaidInfo.versionCode}")
//                    val unlocker = Unlocker(host.getContext())
//                    val unlockerPkg = unlocker.packageInfo
//                    if (unlockerPkg != null) {
//                        Timber.tag(TAG).i("P: VERSIONNAME: ${unlockerPkg.versionName}, VERSIONCODE: ${unlockerPkg.versionCode}")
//                    } else {
//                        Timber.tag(TAG).i("P: VERSIONNAME: -- ; VERSIONCODE: -- ")
//                    }
                }
    }

    @AssistedInject.Factory
    interface Factory : DebugModule.Factory<ApkInfoModule>
}