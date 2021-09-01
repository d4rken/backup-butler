package eu.darken.bb.common.debug.modules

import android.util.Log
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.common.debug.DebugModule
import eu.darken.bb.common.debug.DebugModuleHost
import eu.darken.bb.common.debug.DebugOptions
import eu.darken.bb.common.debug.compareIgnorePath
import eu.darken.rxshell.extra.RXSDebug
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class RXSDebugModule @AssistedInject constructor(
    @Assisted host: DebugModuleHost
) : DebugModule {
    companion object {
        internal val TAG = App.logTag("Debug", "RXSDebug")
    }

    internal val totalShellLaunchCount = AtomicLong()
    internal val processSet: MutableSet<Process> = Collections.newSetFromMap(WeakHashMap())
    internal val sem = Semaphore(1)
    private val processCallback = object : RXSDebug.ProcessCallback {
        override fun onProcessStart(process: Process?) {
            sem.tryAcquire(500, TimeUnit.MILLISECONDS)
            process?.let { processSet.add(it) }
            totalShellLaunchCount.incrementAndGet()

            Timber.tag(TAG).d(
                "Start %s, now %d (total: %d) processes: %s",
                process, processSet.size, totalShellLaunchCount.get(), processSet
            )
            sem.release()
        }

        override fun onProcessEnd(process: Process?) {
            sem.tryAcquire(500, TimeUnit.MILLISECONDS)
            process?.let { processSet.remove(it) }
            Timber.tag(TAG).d(
                "Stop %s, now %d (total: %d) processes: %s",
                process, processSet.size, totalShellLaunchCount.get(), processSet
            )
            sem.release()
        }
    }

    private var previousOptions: DebugOptions = DebugOptions.default()

    init {
        host.observeOptions()
            .observeOn(Schedulers.io())
            .filter { !previousOptions.compareIgnorePath(it) }
            .doOnNext { previousOptions = it }
            .subscribe { options ->
                RXSDebug.setDebug(options.level == Log.VERBOSE && options.isRecording)
                if (options.level == Log.VERBOSE) RXSDebug.addCallback(processCallback)
                else RXSDebug.removeCallback(processCallback)
            }
    }


    @AssistedFactory
    interface Factory : DebugModule.Factory<RXSDebugModule>
}