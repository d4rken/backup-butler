package eu.darken.bb.common

import io.reactivex.disposables.Disposable

data class Operation(
        val label: AString,
        val description: AString? = null,
        val disposable: Disposable
)