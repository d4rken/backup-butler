package eu.darken.bb.common

import io.reactivex.Observable
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.reflect.InvocationTargetException

fun Throwable.getRootCause(): Throwable {
    var error = this
    while (error.cause != null) {
        error = error.cause!!
    }
    if (error is InvocationTargetException) {
        error = error.targetException
    }
    return error
}

fun <T> Observable<T>.mapError(wrapper: (Throwable) -> Throwable): Observable<T> =
        onErrorResumeNext { err: Throwable ->
            Observable.error(wrapper(err))
        }

fun Throwable.getStackTraceString(): String {
    val sw = StringWriter(256)
    val pw = PrintWriter(sw, false)
    printStackTrace(pw)
    pw.flush()
    return sw.toString()
}