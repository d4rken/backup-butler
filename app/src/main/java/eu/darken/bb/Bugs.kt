package eu.darken.bb

import timber.log.Timber

object Bugs {
    fun track(tag: String?, throwable: Throwable) {
        Timber.e(throwable)
        // TODO Bugsnag
    }

    fun track(throwable: Throwable) = this.track(null, throwable)
}