package eu.darken.bb.common.debug.modules

import android.util.Log
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.common.debug.*
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.rxshell.cmd.Cmd
import eu.darken.rxshell.cmd.RxCmdShell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import timber.log.Timber

class EnvPrinter @AssistedInject constructor(
    @Assisted host: DebugModuleHost,
    @DebugScope private val debugScope: CoroutineScope,
) : DebugModule {
    companion object {
        private val TAG = logTag("Debug", "EnvPrinter")
    }

    private var previousOptions: DebugOptions = DebugOptions.default()

    init {
        host.observeOptions()
            .filter { !previousOptions.compareIgnorePath(it) && it.level <= Log.INFO }
            .onEach { previousOptions = it }
            .map { Cmd.builder("printenv").submit(RxCmdShell.builder().build()).blockingGet() }
            .onEach { result ->
                Timber.tag(TAG).d("Environment variables:")
                for (s in result.output) Timber.tag(TAG).d(s)
            }
            .catch { e -> Timber.tag(TAG).e(e, "Failed to get environment variables") }
            .launchIn(debugScope)
    }

    @AssistedFactory
    interface Factory : DebugModule.Factory<EnvPrinter>
}