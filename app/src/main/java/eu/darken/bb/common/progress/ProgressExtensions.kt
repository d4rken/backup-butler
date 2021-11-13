package eu.darken.bb.common.progress

import android.content.Context
import androidx.annotation.StringRes
import eu.darken.bb.common.CaString
import eu.darken.bb.common.toCaString
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

fun <T : Progress.Client> T.updateProgressPrimary(primary: String) {
    updateProgress { it.copy(primary = primary.toCaString()) }
}

fun <T : Progress.Client> T.updateProgressPrimary(primary: CaString) {
    updateProgress { it.copy(primary = primary) }
}

fun <T : Progress.Client> T.updateProgressPrimary(resolv: (Context) -> String) {
    updateProgress { it.copy(primary = resolv.toCaString()) }
}

fun <T : Progress.Client> T.updateProgressPrimary(@StringRes primary: Int, vararg args: Any) {
    updateProgress { state -> state.copy(primary = (primary to args).toCaString()) }
}

fun <T : Progress.Client> T.updateProgressSecondary(secondary: String) {
    updateProgress { it.copy(secondary = secondary.toCaString()) }
}

fun <T : Progress.Client> T.updateProgressSecondary(resolv: (Context) -> String) {
    updateProgress { it.copy(secondary = resolv.toCaString()) }
}

fun <T : Progress.Client> T.updateProgressSecondary(secondary: CaString) {
    updateProgress { it.copy(secondary = secondary) }
}

fun <T : Progress.Client> T.updateProgressSecondary(@StringRes secondary: Int, vararg args: Any) {
    updateProgress { state -> state.copy(secondary = (secondary to args).toCaString()) }
}

fun <T : Progress.Client> T.updateProgressTertiary(tertiary: String) {
    updateProgress { it.copy(tertiary = tertiary.toCaString()) }
}

fun <T : Progress.Client> T.updateProgressTertiary(@StringRes tertiary: Int, vararg args: Any) {
    updateProgress { state -> state.copy(tertiary = (tertiary to args).toCaString()) }
}

fun <T : Progress.Client> T.updateProgressTertiary(resolv: (Context) -> String) {
    updateProgress { it.copy(tertiary = resolv.toCaString()) }
}

fun <T : Progress.Client> T.updateProgressTertiary(tertiary: CaString) {
    updateProgress { it.copy(tertiary = tertiary) }
}

fun <T : Progress.Client> T.updateProgressCount(count: Progress.Count) {
    updateProgress { it.copy(count = count) }
}

suspend fun <T : Progress.Host> T.forwardProgressTo(client: Progress.Client) = progress
    .onCompletion { client.updateProgress { Progress.Data() } }
    .onEach { pro -> client.updateProgress { pro } }
