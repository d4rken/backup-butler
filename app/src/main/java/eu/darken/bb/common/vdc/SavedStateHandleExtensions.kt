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