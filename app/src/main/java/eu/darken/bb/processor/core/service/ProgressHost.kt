package eu.darken.bb.processor.core.service

import androidx.annotation.StringRes
import io.reactivex.Observable

interface ProgressHost {
    data class State(
            val primary: String,
            val secondary: String = "",
            val progressCurrent: Long = 0,
            val progressMax: Long = 0,
            val progressType: Progressable.ProgressType = Progressable.ProgressType.PERCENT
    )

    val progress: Observable<State>
}

interface Progressable {
    fun publishPrimary(@StringRes primary: Int)

    fun publishPrimary(primary: String)

    fun publishSecondary(@StringRes secondary: Int)

    fun publishSecondary(secondary: String)

    fun publishProgress(current: Long, max: Long, progressType: ProgressType = ProgressType.PERCENT)

    enum class ProgressType {
        PERCENT, SIZE, COUNTER
    }

}