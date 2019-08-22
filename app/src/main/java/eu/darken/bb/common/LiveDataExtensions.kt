package eu.darken.bb.common

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <T> LiveData<T>.observe2(owner: LifecycleOwner, callback: (T) -> Unit) {
    observe(owner, Observer { callback.invoke(it) })
}