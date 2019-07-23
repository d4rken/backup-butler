package eu.darken.bb.common

import androidx.lifecycle.LiveData
import eu.darken.bb.common.rx.toLiveData

class StateUpdater<T>(
        startValue: T
) : HotData<T>(startValue) {
    val state: LiveData<T> = data.toLiveData()
}