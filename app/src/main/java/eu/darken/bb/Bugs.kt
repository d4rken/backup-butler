package eu.darken.bb

import androidx.annotation.VisibleForTesting
import com.bugsnag.android.Bugsnag
import timber.log.Timber

object Bugs {
    @VisibleForTesting
    private val testing: Boolean by lazy {
        try {
            Class.forName("testhelper.IsAUnitTest")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    fun track(tag: String?, throwable: Throwable) {
        if (tag != null) {
            Timber.tag(tag).e(throwable)
        } else {
            Timber.e(throwable)
        }
        if (!testing) Bugsnag.notify(throwable)
    }

    fun track(throwable: Throwable) = this.track(null, throwable)
}