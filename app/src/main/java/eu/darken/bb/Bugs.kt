package eu.darken.bb

import timber.log.Timber

object Bugs {
    fun track(throwable: Throwable) {
        Timber.e(throwable)
        // TODO Bugsnag
    }
}