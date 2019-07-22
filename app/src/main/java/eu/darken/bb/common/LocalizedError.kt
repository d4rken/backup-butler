package eu.darken.bb.common

import android.content.Context

interface LocalizedError {
    fun getLocalizedErrorMessage(context: Context): String
}

fun Throwable.tryLocalizedErrorMessage(context: Context): String = when {
    this is LocalizedError -> this.getLocalizedErrorMessage(context)
    else -> localizedMessage
}