package eu.darken.bb.common

import io.reactivex.rxjava3.disposables.Disposable

data class Operation(
    val label: CaString,
    val description: CaString? = null,
    val disposable: Disposable
)