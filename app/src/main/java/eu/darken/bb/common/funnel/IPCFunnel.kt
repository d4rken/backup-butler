package eu.darken.bb.common.funnel

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.debug.logging.logTag
import timber.log.Timber
import java.util.concurrent.Semaphore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tries to reduce the chance that we hit the IPC buffer limit.
 * Hitting the buffer limit can result in crashes or more grave incomplete results.
 */
@Singleton
class IPCFunnel @Inject constructor(
    @ApplicationContext context: Context
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
        internal val TAG = logTag("IPCFunnel")
    }
}
