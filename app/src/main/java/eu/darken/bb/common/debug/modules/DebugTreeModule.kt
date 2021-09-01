package eu.darken.bb.common.debug.modules

import android.util.Log
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.debug.DebugModule
import eu.darken.bb.common.debug.DebugModuleHost
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class DebugTreeModule @AssistedInject constructor(
    @Assisted host: DebugModuleHost
) : DebugModule {

    private var debugTree: Timber.DebugTree? = null

    init {
        host.observeOptions()
            .observeOn(Schedulers.io())
            .subscribe { options ->
                if (options.level == Log.VERBOSE && debugTree == null) {
                    debugTree = Timber.DebugTree()
                    debugTree?.let {
                        Timber.plant(it)
                    }
                } else if (options.level != Log.VERBOSE && debugTree != null) {
                    debugTree?.let {
                        Timber.uproot(it)
                        debugTree = null
                    }
                }
            }
    }

    @AssistedInject.Factory
    interface Factory : DebugModule.Factory<DebugTreeModule>
}