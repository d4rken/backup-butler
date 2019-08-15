package eu.darken.bb.common

data class Opt<out T>(val value: T? = null) {
    val isNotNull: Boolean = value != null
    val isNull: Boolean = value == null

    fun notNullValue(errorMessage: String? = null): T {
        if (value == null) {
            throw IllegalStateException(errorMessage ?: "Value shouldn't be null")
        }
        return value
    }
}

fun <T> T?.opt() = Opt(this)