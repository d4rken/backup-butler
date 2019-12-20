package eu.darken.bb.common.shell

import eu.darken.bb.common.SharedHolder
import eu.darken.rxshell.cmd.RxCmdShell
import timber.log.Timber

class SharedShell constructor(tag: String) : SharedHolder.HasKeepAlive<RxCmdShell.Session> {

    val tag = "$tag:SharedShell"

    val session = SharedHolder<RxCmdShell.Session>(tag) { emitter ->
        Timber.tag(tag).d("Initiating connection to host.")

        val session = try {
            RxCmdShell.builder().build().open().blockingGet()
        } catch (e: Exception) {
            emitter.onError(e)
            return@SharedHolder
        }

        emitter.setCancellable {
            Timber.tag(tag).d("Canceling!")
            session.close().subscribe()
        }

        emitter.onAvailable(session)

        try {
            val end = session.waitFor().blockingGet()
            if (end != 0) {
                emitter.onError(IllegalStateException("SharedShell finished with exitcode $end"))
            } else {
                emitter.onEnd()
            }
        } catch (e: Exception) {
            emitter.onError(e)
        }
    }

    override val keepAlive: SharedHolder<RxCmdShell.Session> = session

}