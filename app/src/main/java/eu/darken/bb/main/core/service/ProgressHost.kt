package eu.darken.bb.main.core.service

import androidx.annotation.StringRes
import io.reactivex.Observable

interface ProgressHost {
    data class State(
            val primary: String
    )

    val progress: Observable<State>
}

interface Progressable {
    fun publishPrimary(@StringRes primary: Int)

    fun publishPrimary(primary: String)

}