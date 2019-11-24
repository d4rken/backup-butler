package eu.darken.bb.common

import kotlin.reflect.KClass

fun Throwable.unwrapIf(clazz: KClass<*>): Throwable {
    return if (clazz.isInstance(this) && this.cause != null) {
        this.cause!!
    } else {
        this
    }
}