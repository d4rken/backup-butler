package eu.darken.bb.common.debug.modules

import android.util.Log
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.common.debug.DebugModule
import eu.darken.bb.common.debug.DebugModuleHost
import eu.darken.bb.common.debug.DebugOptions
import eu.darken.bb.common.debug.compareIgnorePath
import eu.darken.rxshell.cmd.Cmd
import eu.darken.rxshell.cmd.RxCmdShell
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class EnvPrinter @AssistedInject constructor(
        @Assisted host: DebugModuleHost
) : DebugModule {
    companion object {
        private val TAG = App.logTag("Debug", "EnvPrinter")
    }

    private var previousOptions: DebugOptions = DebugOptions.default()

    init {
        host.observeOptions()
                .observeOn(Schedulers.io())
                .filter { !previousOptions.compareIgnorePath(it) && it.level <= Log.INFO }
                .doOnNext { previousOptions = it }
                .flatMapSingle { Cmd.builder("printenv").submit(RxCmdShell.builder().build()) }
                .subscribe(
                        { result ->
                            Timber.tag(TAG).d("Environment variables:")
                            for (s in result.output) Timber.tag(TAG).d(s)
                        },
                        { e -> Timber.tag(TAG).e(e, "Failed to get environment variables") }
                )
    }

    @AssistedInject.Factory
    interface Factory : DebugModule.Factory<EnvPrinter>
}