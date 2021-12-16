package eu.darken.bb.common.funnel

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tries to reduce the chance that we hit the IPC buffer limit.
 * Hitting the buffer limit can result in crashes or more grave incomplete results.
 */
@Singleton
class IPCFunnel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatcherProvider: DispatcherProvider
) {
    private val funnelLock = Mutex()
    private val packageManager: PackageManager = context.packageManager

    init {
        Timber.tag(TAG).d("IPCFunnel initialized.")
    }

    suspend fun <T> use(block: FunnelEnvironment.() -> T): T = withContext(dispatcherProvider.IO) {
        funnelLock.withLock {
            val env = object : FunnelEnvironment {
                override val packageManager: PackageManager
                    get() = this@IPCFunnel.packageManager
            }

            block(env)
        }
    }

    interface FunnelEnvironment {
        val packageManager: PackageManager
    }

    companion object {
        internal val TAG = logTag("IPCFunnel")
    }
}
