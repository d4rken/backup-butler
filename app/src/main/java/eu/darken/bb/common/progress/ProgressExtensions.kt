package eu.darken.bb.common.progress

import android.content.Context
import androidx.annotation.StringRes
import eu.darken.bb.common.AString
import eu.darken.bb.common.CAString

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
    updateProgress { state -> state.copy(primary = CAString(secondary, *args)) }
}

fun <T : Progress.Client> T.updateProgressTertiary(tertiary: String) {
    updateProgress { it.copy(tertiary = CAString { tertiary }) }
}

fun <T : Progress.Client> T.updateProgressTertiary(@StringRes tertiary: Int, vararg args: Any) {
    updateProgress { state -> state.copy(tertiary = CAString(tertiary, *args)) }
}

fun <T : Progress.Client> T.updateProgressCount(count: Progress.Count) {
    updateProgress { it.copy(count = count) }
}