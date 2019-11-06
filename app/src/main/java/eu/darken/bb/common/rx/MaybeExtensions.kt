package eu.darken.bb.common.rx

import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.internal.functions.Functions


fun <T> Maybe<T>.subscribeNullable(
        callback: (T?) -> Unit
): Disposable = subscribeNullable(callback, { Functions.ON_ERROR_MISSING.accept(it) })

fun <T> Maybe<T>.subscribeNullable(
        callback: (T?) -> Unit,
        onError: (Throwable) -> Unit
): Disposable = subscribe(
        { callback(it) },
        { onError(it) },
        { callback(null) }
)

fun <T> Maybe<T>.blockingGet2(): T? = blockingGet()

fun <T> Maybe<T>.singleOrError(throwable: Throwable): Single<T> =
        switchIfEmpty(Single.error(throwable))