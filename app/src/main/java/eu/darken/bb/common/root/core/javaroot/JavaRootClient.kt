package eu.darken.bb.common.root.core.javaroot

import android.content.Context
import eu.darken.bb.App
import eu.darken.bb.common.SharedResource
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.root.core.javaroot.fileops.ClientModule
import eu.darken.bb.common.root.core.javaroot.fileops.FileOpsClient
import eu.darken.bb.common.root.librootjava.RootIPCReceiver
import eu.darken.bb.common.root.librootjava.RootJava
import eu.darken.bb.common.unwrapIf
import eu.darken.rxshell.cmd.Cmd
import eu.darken.rxshell.cmd.RxCmdShell
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@PerApp
class JavaRootClient @Inject constructor(
        @AppContext private val context: Context
) {

    data class Connection(
            val ipc: JavaRootConnection,
            val clientModules: List<ClientModule>
    )

    val sharedResource = SharedResource<Connection>("$TAG:SharedResource") { emitter ->
        Timber.tag(TAG).d("Initiating connection to host.")

        val rootSession = try {
            RxCmdShell.builder().root(true).build().open().blockingGet()
        } catch (e: Exception) {
            emitter.onError(e.unwrapIf(RuntimeException::class))
            return@SharedResource
        }

        val ipcReceiver = object : RootIPCReceiver<JavaRootConnection>(context, 0) {
            override fun onConnect(ipc: JavaRootConnection) {
                Timber.tag(TAG).d("onConnect(ipc=%s)", ipc)

                emitter.onAvailable(Connection(
                        ipc = ipc,
                        clientModules = listOf(FileOpsClient(ipc.fileOps))
                ))
            }

            override fun onDisconnect(ipc: JavaRootConnection) {
                Timber.tag(TAG).d("onDisconnect(ipc=%s)", ipc)
                emitter.onEnd()
            }
        }
        emitter.setCancellable {
            Timber.tag(TAG).d("Canceling!")
            ipcReceiver.release()
            // TODO timeout until we CANCEL?
            rootSession.close().subscribe()
            RootJava.cleanupCache(context)
        }

        try {
            val script = JavaRootHost.getLaunchScript(context)
            Cmd.builder(script).submit(rootSession).subscribe()
        } catch (e: Exception) {
            emitter.onError(e.unwrapIf(RuntimeException::class))
        }
    }

    @Throws(IOException::class)
    fun <T> runSessionAction(action: (Connection) -> T): T {
        try {
            sharedResource.getResource().use {
                return action(it.resource)
            }
        } catch (e: Exception) {
            throw e.unwrapIf(RuntimeException::class)
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(IOException::class)
    fun <R, T> runModuleAction(moduleClass: Class<out R>, action: (R) -> T): T {
        return runSessionAction { session ->
            val module = session.clientModules.single { moduleClass.isInstance(it) } as R
            return@runSessionAction action(module)
        }
    }

    companion object {
        val TAG = App.logTag("Root", "Java", "Client")
    }
}