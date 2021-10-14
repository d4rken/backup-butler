package eu.darken.bb.common.errors

import android.content.Context
import eu.darken.bb.common.R

interface HasLocalizedError {
    fun getLocalizedError(context: Context): LocalizedError
}

data class LocalizedError(
    val throwable: Throwable,
    val label: String,
    val description: String
) {
    fun asText() = "$label:\n$description"
}

fun Throwable.localized(c: Context): LocalizedError = when {
    this is HasLocalizedError -> this.getLocalizedError(c)
    localizedMessage != null -> LocalizedError(
        throwable = this,
        label = "${c.getString(R.string.general_error_label)}: ${this::class.simpleName!!}",
        description = localizedMessage!!.lines()
            .filterIndexed { index, _ -> index > 1 }
            .take(3)
            .joinToString("\n")
    )
    else -> LocalizedError(
        throwable = this,
        label = "${c.getString(R.string.general_error_label)}: ${this::class.simpleName!!}",
        description = this.stackTraceToString()
            .lines()
            .filterIndexed { index, _ -> index > 1 }
            .take(3)
            .joinToString("\n")
    )
}

