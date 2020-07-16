package eu.darken.bb.common.rx

import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.internal.functions.Functions


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