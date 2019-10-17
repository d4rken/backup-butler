package eu.darken.bb.common

import android.content.Context
import eu.darken.bb.R

class TypeMissMatchException(private val expected: Any, private val actual: Any)
    : IllegalArgumentException("Type missmatch: Wanted $expected, but got $actual."), LocalizedError {
    override fun getLocalizedErrorMessage(context: Context): String {
        return context.getString(R.string.error_msg_type_missmatch, expected, actual)
    }

    companion object {
        fun check(expected: Any, actual: Any) {
            if (expected != actual) throw TypeMissMatchException(expected, actual)
        }
    }

}