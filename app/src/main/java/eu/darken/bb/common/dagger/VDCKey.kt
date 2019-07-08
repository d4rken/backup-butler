package eu.darken.bb.common.dagger

import dagger.MapKey
import eu.darken.bb.common.VDC
import kotlin.reflect.KClass


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class VDCKey(val value: KClass<out VDC>)