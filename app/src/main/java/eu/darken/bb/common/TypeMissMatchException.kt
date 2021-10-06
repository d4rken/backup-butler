package eu.darken.bb.common

import android.content.Context
import eu.darken.bb.R

class TypeMissMatchException(private val expected: Any, private val actual: Any) :
    IllegalArgumentException("Type missmatch: Wanted $expected, but got $actual."), HasLocalizedError {

    override fun getLocalizedError(context: Context) = LocalizedError(
        throwable = this,
        label = "TypeMissMatchException",
        description = context.getString(R.string.general_error_type_missmatch_msg, expected, actual)
    )

    companion object {
        fun check(expected: Any, actual: Any) {
            if (expected != actual) throw TypeMissMatchException(expected, actual)
        }
    }
}