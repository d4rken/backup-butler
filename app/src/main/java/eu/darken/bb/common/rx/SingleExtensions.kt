package eu.darken.bb.common.rx

import androidx.lifecycle.LiveData
import eu.darken.bb.common.Opt
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single


fun <T : Any> Single<T>.blockingGetUnWrapped(): T {
    try {
        return blockingGet()
    } catch (e: Throwable) {
        if (e is RuntimeException && e.cause != null) {
            throw e.cause!!
        } else {
            throw e
        }
    }
}

fun <T, W : Opt<T>> Single<W>.optToMaybe(): Maybe<T> {
    return flatMapMaybe { if (it.isNull) Maybe.empty() else Maybe.just(it.value!!) }
}

fun <T : Any> Single<T>.asLiveData(): LiveData<T> = this.toObservable().asLiveData()