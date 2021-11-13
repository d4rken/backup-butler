package eu.darken.bb.common.debug.modules

import android.annotation.TargetApi
import android.os.Build
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.BuildConfig
import eu.darken.bb.common.ApiHelper
import eu.darken.bb.common.debug.DebugModule
import eu.darken.bb.common.debug.DebugModuleHost
import eu.darken.bb.common.debug.DebugScope
import eu.darken.bb.common.debug.logging.logTag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@TargetApi(Build.VERSION_CODES.N)
class ACSDebugModule @AssistedInject constructor(
    @Assisted host: DebugModuleHost,
    @DebugScope private val debugScope: CoroutineScope,
) : DebugModule {

    companion object {
        internal val TAG = logTag("Debug", "ACSDebugModule")
    }

    private var crashHandler: Thread.UncaughtExceptionHandler? = null
    private var origHandler: Thread.UncaughtExceptionHandler? = null

    init {
        host.observeOptions()
            .filter { BuildConfig.DEBUG }
            .filter { ApiHelper.hasAndroidN() }
            .onEach { options ->
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
            .launchIn(debugScope)
    }

    @AssistedFactory
    interface Factory : DebugModule.Factory<ACSDebugModule>
}