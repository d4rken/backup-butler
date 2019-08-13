package eu.darken.bb.common.progress

import android.content.Context
import androidx.annotation.StringRes
import eu.darken.bb.common.HasContext

fun <T : Progress.Client> T.updateProgressPrimary(primary: String) {
    updateProgress { it.copy(primary = primary) }
}

fun <T : Progress.Client> T.updateProgressPrimary(context: Context, @StringRes primary: Int, vararg args: Any) {
    updateProgress { it.copy(primary = context.getString(primary, *args)) }
}

fun <T> T.updateProgressPrimary(@StringRes primary: Int, vararg args: Any) where T : Progress.Client, T : HasContext {
    updateProgress { it.copy(primary = context.getString(primary, *args)) }
}

fun <T : Progress.Client> T.updateProgressSecondary(secondary: String) {
    updateProgress { it.copy(secondary = secondary) }
}

fun <T : Progress.Client> T.updateProgressSecondary(context: Context, @StringRes secondary: Int, vararg args: Any) {
    updateProgress { it.copy(secondary = context.getString(secondary, *args)) }
}

fun <T> T.updateProgressSecondary(@StringRes secondary: Int, vararg args: Any) where T : Progress.Client, T : Context {
    updateProgress { it.copy(secondary = getString(secondary, *args)) }
}

fun <T : Progress.Client> T.updateProgressTertiary(tertiary: String) {
    updateProgress { it.copy(tertiary = tertiary) }
}

fun <T> T.updateProgressTertiary(@StringRes tertiary: Int) where T : Progress.Client, T : Context {
    updateProgress { it.copy(tertiary = getString(tertiary)) }
}

fun <T : Progress.Client> T.updateProgressCount(count: Progress.Count) {
    updateProgress { it.copy(count = count) }
}