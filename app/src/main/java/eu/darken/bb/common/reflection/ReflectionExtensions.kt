package eu.darken.bb.common.reflection

@Suppress("UNCHECKED_CAST")
inline fun <reified CLASS : Any, TYPE> CLASS.getPrivateProperty(name: String): TYPE? = javaClass
    .getDeclaredField(name)
    .let { field ->
        field.isAccessible = true
        return@let field.get(this) as? TYPE
    }

inline fun <reified CLASS : Any, TYPE> CLASS.setPrivateProperty(name: String, data: TYPE) = javaClass
    .getDeclaredField(name)
    .let { field ->
        field.isAccessible = true
        field.set(this, data)
    }