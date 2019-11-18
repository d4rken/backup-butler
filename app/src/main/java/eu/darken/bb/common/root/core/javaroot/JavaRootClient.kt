package eu.darken.bb.common.root.core.javaroot

import android.content.Context
import eu.darken.bb.App
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.root.librootjava.RootIPCReceiver
import eu.darken.bb.common.root.librootjava.RootJava
import eu.darken.rxshell.cmd.Cmd
import eu.darken.rxshell.cmd.RxCmdShell
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

@PerApp
class JavaRootClient @Inject constructor(
        @AppContext private val context: Context
) {
    data class Session(
            val ipc: JavaRootConnection
    )

    val session: Observable<Session> = Observable
            .create<Session> { emitter ->
                Timber.tag(TAG).d("Initiating connection to host.")

                val rootSession = try {
                    RxCmdShell.builder().root(true).build().open().blockingGet()
                } catch (e: Exception) {
                    emitter.tryOnError(e)
                    return@create
                }

                val ipcReceiver = object : RootIPCReceiver<JavaRootConnection>(context, 0) {
                    override fun onConnect(ipc: JavaRootConnection) {
                        Timber.tag(TAG).d("onConnect(ipc=%s)", ipc)
                        emitter.onNext(Session(ipc))
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
            .doFinally { Timber.tag(TAG).d("doFinally()") }
            .replay(1).refCount()


    companion object {
        val TAG = App.logTag("Root", "Java", "Client")
    }
}