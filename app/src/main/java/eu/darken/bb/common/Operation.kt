package eu.darken.bb.common

import io.reactivex.rxjava3.disposables.Disposable

data class Operation(
    val label: AString,
    val description: AString? = null,
    val disposable: Disposable
)