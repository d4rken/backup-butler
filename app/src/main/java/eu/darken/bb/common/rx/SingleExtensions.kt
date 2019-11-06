package eu.darken.bb.common.rx

import eu.darken.bb.common.Opt
import io.reactivex.Maybe
import io.reactivex.Single


fun <T> Single<T>.blockingGetUnWrapped(): T {
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
    return flatMapMaybe { if (it.isNull) Maybe.empty() else Maybe.just(it.value) }
}