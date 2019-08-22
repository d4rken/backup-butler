package eu.darken.bb.common

import android.os.FileObserver
import io.reactivex.Observable
import java.io.File

class JavaFileObserver(private val path: File, private val mask: Int = 0) {
    private lateinit var obs: FileObserver
    val events: Observable<Event> = Observable
            .create<Event> { emitter ->
                obs = object : FileObserver(path.canonicalPath, mask) {
                    override fun onEvent(event: Int, path: String?) {
                        emitter.onNext(Event(event, path?.let { File(it) }))
                    }
                }
                emitter.setCancellable {
                    obs.stopWatching()
                    emitter.onComplete()
                }
                obs.startWatching()
            }
            .share()

    data class Event(val type: Int, val path: File?)

}