package eu.darken.bb.common.debug.modules

import android.annotation.TargetApi
import android.os.Build
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.BuildConfig
import eu.darken.bb.common.ApiHelper
import eu.darken.bb.common.debug.DebugModule
import eu.darken.bb.common.debug.DebugModuleHost
import io.reactivex.schedulers.Schedulers

@TargetApi(Build.VERSION_CODES.N)
class ACSDebugModule @AssistedInject constructor(
        @Assisted host: DebugModuleHost
) : DebugModule {

    companion object {
        internal val TAG = App.logTag("Debug", "ACSDebugModule")
    }

    private var crashHandler: Thread.UncaughtExceptionHandler? = null
    private var origHandler: Thread.UncaughtExceptionHandler? = null

    init {
        host.observeOptions()
                .observeOn(Schedulers.io())
                .filter { BuildConfig.DEBUG }
                .filter { ApiHelper.hasAndroidN() }
                .subscribe { options ->
                    //                    if (options.level == Log.VERBOSE && crashHandler == null) {
//                        origHandler = Thread.getDefaultUncaughtExceptionHandler()
//                        crashHandler = Thread.UncaughtExceptionHandler { t, e ->
//                            Timber.tag(TAG).e(e, "Disabling ACCService to guard against exception")
//                            ACCService.instance?.disableSelf()
//                            origHandler?.uncaughtException(t, e)
//                        }
//                        Thread.setDefaultUncaughtExceptionHandler(crashHandler)
//                        Timber.tag(TAG).d("Guarding ACCService against exceptions")
//                    } else if (options.level != Log.VERBOSE && crashHandler != null) {
//                        Thread.setDefaultUncaughtExceptionHandler(origHandler)
//                        Timber.tag(TAG).d("Disabled ACCService exception guard")
//                    }
                }
    }

    @AssistedInject.Factory
    interface Factory : DebugModule.Factory<ACSDebugModule>

}