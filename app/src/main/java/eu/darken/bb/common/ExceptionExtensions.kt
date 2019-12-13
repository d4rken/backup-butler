package eu.darken.bb.common

import kotlin.reflect.KClass

fun Throwable.hasCause(exceptionClazz: KClass<out Throwable>): Boolean {
    return exceptionClazz.isInstance(this.getRootCause())
}