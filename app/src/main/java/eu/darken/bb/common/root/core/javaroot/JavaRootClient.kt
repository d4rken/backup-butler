package eu.darken.bb.common.root.core.javaroot

import android.content.Context
import eu.darken.bb.App
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.getRootCause
import eu.darken.bb.common.root.core.javaroot.fileops.ClientModule
import eu.darken.bb.common.root.core.javaroot.fileops.FileOpsClient
import eu.darken.bb.common.root.librootjava.RootIPCReceiver
import eu.darken.bb.common.root.librootjava.RootJava
import eu.darken.rxshell.cmd.Cmd
import eu.darken.rxshell.cmd.RxCmdShell
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.io.Closeable
import java.io.IOException
import javax.inject.Inject

@PerApp
class JavaRootClient @Inject constructor(
        @AppContext private val context: Context
) {
    data class Session(
            val connection: Connection,
            private val disposable: Disposable
    ) : Closeable, AutoCloseable {

        override fun close() = disposable.dispose()

        data class Connection(
                val ipc: JavaRootConnection,
                val clientModules: List<ClientModule>
        )
    }

    private val connectionObs: Observable<Session.Connection> = Observable
            .create<Session.Connection> { emitter ->
                Timber.tag(TAG).d("Initiating connection to host.")

                val rootSession = try {
                    RxCmdShell.builder().root(true).build().open().blockingGet()
                } catch (e: Exception) {
                    emitter.tryOnError(e.cause as? IOException ?: e)
                    return@create
                }

                val ipcReceiver = object : RootIPCReceiver<JavaRootConnection>(context, 0) {
                    override fun onConnect(ipc: JavaRootConnection) {
                        Timber.tag(TAG).d("onConnect(ipc=%s)", ipc)

                        emitter.onNext(Session.Connection(
                                ipc = ipc,
                                clientModules = listOf(FileOpsClient(ipc.fileOps))
                        ))
                    }

                    override fun onDisconnect(ipc: JavaRootConnection) {
                        Timber.tag(TAG).d("onDisconnect(ipc=%s)", ipc)
                        emitter.onComplete()
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
                    emitter.tryOnError(e)
                }
            }
            .doFinally {
                Timber.tag(TAG).d("doFinally()")
                activeTokens.clear()
            }
            .replay(1).refCount()

    internal val activeTokens = mutableSetOf<Disposable>()

    @Throws(IOException::class)
    fun getASession(): Session {
        lateinit var keepAlive: Disposable

        val connection = connectionObs
                .doOnSubscribe { keepAlive = it }
                .blockingFirst()

        activeTokens.add(keepAlive)
        Timber.tag(TAG).d("Adding token, now: %d", activeTokens.size)

        val wrappedKeepAlive = object : Disposable {
            override fun isDisposed(): Boolean {
                return keepAlive.isDisposed
            }

            override fun dispose() {
                activeTokens.remove(keepAlive)
                Timber.tag(TAG).d("Removing token, now: %d", activeTokens.size)
                keepAlive.dispose()
            }
        }

        return Session(connection, wrappedKeepAlive)
    }

    @Throws(IOException::class)
    fun <T> runSessionAction(action: (Session.Connection) -> T): T {
        try {
            getASession().use {
                return action(it.connection)
            }
        } catch (e: Exception) {
            throw e.getRootCause()
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