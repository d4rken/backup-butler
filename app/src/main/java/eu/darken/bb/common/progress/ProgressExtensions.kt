package eu.darken.bb.common.progress

import android.content.Context
import androidx.annotation.StringRes
import eu.darken.bb.common.AString
import eu.darken.bb.common.CAString
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

fun <T : Progress.Client> T.updateProgressPrimary(primary: String) {
    updateProgress { it.copy(primary = CAString(primary)) }
}

fun <T : Progress.Client> T.updateProgressPrimary(primary: AString) {
    updateProgress { it.copy(primary = primary) }
}

fun <T : Progress.Client> T.updateProgressPrimary(resolv: (Context) -> String) {
    updateProgress { it.copy(primary = CAString(resolv)) }
}

fun <T : Progress.Client> T.updateProgressPrimary(@StringRes primary: Int, vararg args: Any) {
    updateProgress { state -> state.copy(primary = CAString(primary, *args)) }
}

fun <T : Progress.Client> T.updateProgressSecondary(secondary: String) {
    updateProgress { it.copy(secondary = CAString(secondary)) }
}

fun <T : Progress.Client> T.updateProgressSecondary(resolv: (Context) -> String) {
    updateProgress { it.copy(secondary = CAString(resolv)) }
}

fun <T : Progress.Client> T.updateProgressSecondary(secondary: AString) {
    updateProgress { it.copy(secondary = secondary) }
}

fun <T : Progress.Client> T.updateProgressSecondary(@StringRes secondary: Int, vararg args: Any) {
    updateProgress { state -> state.copy(secondary = CAString(secondary, *args)) }
}

fun <T : Progress.Client> T.updateProgressTertiary(tertiary: String) {
    updateProgress { it.copy(tertiary = CAString { tertiary }) }
}

fun <T : Progress.Client> T.updateProgressTertiary(@StringRes tertiary: Int, vararg args: Any) {
    updateProgress { state -> state.copy(tertiary = CAString(tertiary, *args)) }
}

fun <T : Progress.Client> T.updateProgressTertiary(resolv: (Context) -> String) {
    updateProgress { it.copy(tertiary = CAString(resolv)) }
}

fun <T : Progress.Client> T.updateProgressTertiary(tertiary: AString) {
    updateProgress { it.copy(tertiary = tertiary) }
}

fun <T : Progress.Client> T.updateProgressCount(count: Progress.Count) {
    updateProgress { it.copy(count = count) }
}

fun <T : Progress.Host> T.forwardProgressTo(client: Progress.Client): Disposable {
    return progress
        .subscribeOn(Schedulers.io())
        .doFinally { client.updateProgress { Progress.Data() } }
        .subscribe { pro -> client.updateProgress { pro } }
}