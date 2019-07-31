package eu.darken.bb.common

data class Opt<out T>(val value: T?) {
    val isNotNull: Boolean = value != null
    val isNull: Boolean = value == null

    fun notNullValue(): T {
        if (value == null) {
            throw IllegalStateException("Value shouldn't be null")
        }
        return value
    }
}

fun <T> T?.opt() = Opt(this)