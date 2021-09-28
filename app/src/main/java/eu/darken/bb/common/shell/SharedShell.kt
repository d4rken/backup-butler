package eu.darken.bb.common.shell

import eu.darken.bb.common.SharedHolder
import eu.darken.rxshell.cmd.RxCmdShell
import io.reactivex.rxjava3.core.Observable
import timber.log.Timber

class SharedShell constructor(tag: String) : SharedHolder.HasKeepAlive<RxCmdShell.Session> {
    private val aTag = "$tag:SharedShell"
    private val source = Observable.create<RxCmdShell.Session> { emitter ->
        Timber.tag(aTag).d("Initiating connection to host.")

        val session = try {
            RxCmdShell.builder().build().open().blockingGet()
        } catch (e: Exception) {
            emitter.onError(e)
            return@create
        }

        emitter.setCancellable {
            Timber.tag(aTag).d("Canceling!")
            session.close().subscribe()
        }

        emitter.onNext(session)

        val end = try {
            session.waitFor().blockingGet()
        } catch (sessionError: Exception) {
            emitter.tryOnError(IllegalStateException("SharedShell finished unexpectedly", sessionError))
            return@create
        }

        if (end != 0) {
            emitter.tryOnError(IllegalStateException("SharedShell finished with exitcode $end"))
        } else {
            emitter.onComplete()
        }
    }

    val session = SharedHolder<RxCmdShell.Session>(aTag, source)

    override val keepAlive: SharedHolder<RxCmdShell.Session> = session

}