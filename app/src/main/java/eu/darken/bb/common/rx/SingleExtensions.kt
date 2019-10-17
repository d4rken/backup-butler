package eu.darken.bb.common.rx

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