package eu.darken.bb.common.funnel

import android.content.Context
import android.content.pm.PackageManager
import eu.darken.bb.App
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import timber.log.Timber
import java.util.concurrent.Semaphore
import javax.inject.Inject

/**
 * Tries to reduce the chance that we hit the IPC buffer limit.
 * Hitting the buffer limit can result in crashes or more grave incomplete results.
 */
@PerApp
class IPCFunnel @Inject constructor(
        @AppContext context: Context
) {
    private val funnelLock = Semaphore(1)
    private val packageManager: PackageManager = context.packageManager

    init {
        Timber.tag(TAG).d("IPCFunnel initialized.")
    }

    fun <T> queryPM(query: (PackageManager) -> T): T {
        try {
            funnelLock.acquire()
            return query(packageManager)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } finally {
            funnelLock.release()
        }
    }

    companion object {
        internal val TAG = App.logTag("IPCFunnel")
    }
}
