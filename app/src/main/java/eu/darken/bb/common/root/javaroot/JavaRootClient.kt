package eu.darken.bb.common.root.javaroot

import eu.darken.bb.common.SharedResource
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.debug.logging.Logging.Priority.*
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.local.root.ClientModule
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JavaRootClient @Inject constructor(
    javaRootHostLauncher: JavaRootHostLauncher,
    @AppScope coroutineScope: CoroutineScope
) : SharedResource<JavaRootClient.Connection>(
    TAG,
    coroutineScope,
    javaRootHostLauncher.create()
) {

    data class Connection(
        val ipc: JavaRootConnection,
        val clientModules: List<ClientModule>
    ) {
        inline fun <reified T> getModule(): T {
            return clientModules.single { it is T } as T
        }
    }

    suspend fun <T> runSessionAction(action: (Connection) -> T): T = get().use {
        log(TAG, VERBOSE) { "runSessionAction(action=$action)" }
        return action(it.item)
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun <R, T> runModuleAction(moduleClass: Class<out R>, action: (R) -> T): T = runSessionAction { session ->
        log(TAG, VERBOSE) { "runModuleAction(moduleClass=$moduleClass, action=$action)" }
        val module = session.clientModules.single { moduleClass.isInstance(it) } as R
        return@runSessionAction action(module)
    }

    companion object {
        private val TAG = logTag("Root", "Java", "Client")
    }
}