package eu.darken.bb.common.error

import io.reactivex.rxjava3.core.Observable
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass

val Throwable.causes: Sequence<Throwable>
    get() = sequence {
        var subCause = cause
        while (subCause != null) {
            yield(subCause)
            subCause = subCause.cause
        }
    }

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

fun Throwable.hasCause(exceptionClazz: KClass<out Throwable>): Boolean {
    return exceptionClazz.isInstance(this.getRootCause())
}

fun <T : Any> Observable<T>.mapError(wrapper: (Throwable) -> Throwable): Observable<T> =
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

fun Throwable.tryUnwrap(kClass: KClass<RuntimeException> = RuntimeException::class): Throwable =
    if (!kClass.isInstance(this)) this else cause ?: this