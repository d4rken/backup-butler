package eu.darken.bb.common.root.core.javaroot

import android.content.Context
import eu.darken.bb.App
import eu.darken.bb.common.SharedHolder
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.files.core.local.root.ClientModule
import eu.darken.bb.common.files.core.local.root.FileOpsClient
import eu.darken.bb.common.pkgs.pkgops.root.PkgOpsClient
import eu.darken.bb.common.root.librootjava.RootIPCReceiver
import eu.darken.bb.common.root.librootjava.RootJava
import eu.darken.rxshell.cmd.Cmd
import eu.darken.rxshell.cmd.RxCmdShell
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@PerApp
class JavaRootClient @Inject constructor(
    @AppContext private val context: Context
) : SharedHolder<JavaRootClient.Connection>(
    TAG, connectionSourcer(context)
) {

    data class Connection(
        val ipc: JavaRootConnection,
        val clientModules: List<ClientModule>
    ) {
        inline fun <reified T> getModule(): T {
            return clientModules.single { it is T } as T
        }
    }

    fun <T> runSessionAction(action: (Connection) -> T): T {
        get().use {
            return action(it.item)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <R, T> runModuleAction(moduleClass: Class<out R>, action: (R) -> T): T {
        return runSessionAction { session ->
            val module = session.clientModules.single { moduleClass.isInstance(it) } as R
            return@runSessionAction action(module)
        }
    }

    companion object {
        val TAG = App.logTag("Root", "Java", "Client")

        internal fun connectionSourcer(context: Context): (ResourceEmitter<Connection>) -> Unit = gen@{ emitter ->
            Timber.tag(TAG).d("Initiating connection to host.")

            val rootSession = try {
                RxCmdShell.builder().root(true).build().open().blockingGet()
            } catch (e: Exception) {
                emitter.onError(RootException("Failed to open root session.", e.cause))
                return@gen
            }

            val ipcReceiver = object : RootIPCReceiver<JavaRootConnection>(context, 0) {
                override fun onConnect(ipc: JavaRootConnection) {
                    Timber.tag(TAG).d("onConnect(ipc=%s)", ipc)

                    emitter.onAvailable(
                        Connection(
                            ipc = ipc,
                            clientModules = listOf(
                                FileOpsClient(ipc.fileOps),
                                PkgOpsClient(ipc.pkgOps)
                            )
                        )
                    )
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
                // Doesn't return until root host has quit
                val result = Cmd.builder(script).submit(rootSession).observeOn(Schedulers.io()).blockingGet()
                Timber.tag(TAG).d("Root host launch result was: %s", result)
                // Check exitcode
                if (result.exitCode == Cmd.ExitCode.SHELL_DIED) {
                    emitter.onError(RootException("Shell died launching the java root host."))
                }
            } catch (e: Exception) {
                emitter.onError(RootException("Failed to launch java root host.", e.cause))
            }
        }
    }
}