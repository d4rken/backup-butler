package eu.darken.bb.common.vdc

import androidx.lifecycle.SavedStateHandle

fun SavedStateHandle.ifFresh(action: () -> Unit): Boolean {
    val key = "eu.darken.bb.common.vdc.ifFresh"
    val fresh = get<Boolean>(key) ?: true
    if (fresh) {
        action.invoke()
        set(key, false)
    }
    return fresh
}

fun SavedStateHandle.asLog(): String {
    val valueMap = keys().map { it to get<Any>(it) }.joinToString { (key, value) -> "$key=$value" }
    return "SavedStateHandle($this):\n$valueMap"
}