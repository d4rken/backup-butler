package eu.darken.bb.common

import android.content.Context
import androidx.annotation.StringRes

interface HasContext {
    val context: Context
}

fun HasContext.getString(@StringRes stringRes: Int, vararg args: Any): String {
    return context.getString(stringRes, *args)
}