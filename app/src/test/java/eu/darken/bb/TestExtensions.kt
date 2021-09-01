package eu.darken.bb

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import io.kotest.matchers.collections.shouldContainAll
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

inline fun <reified T> PolymorphicJsonAdapterFactory<T>.testAllSubTypesRegistered() {
    val subtypes = PolymorphicJsonAdapterFactory::class.memberProperties
        .single { it.name == "subtypes" }
        .let {
            it.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            it as KProperty<List<T>>
        }
    val registeredTypes = subtypes.getter.call(this) as List<Class<out T>>
    val expectedTypes = T::class.sealedSubclasses.map { it.java }
    registeredTypes shouldContainAll expectedTypes
}