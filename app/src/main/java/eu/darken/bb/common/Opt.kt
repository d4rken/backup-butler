package eu.darken.bb.common

data class Opt<out T>(val value: T?) {
    val isNotNull: Boolean = value != null
    val isNull: Boolean = value == null
}

fun <T> T?.opt() = Opt(this)