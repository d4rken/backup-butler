package eu.darken.bb.common.debug.bugsnag


import com.bugsnag.android.Event
import com.bugsnag.android.OnErrorCallback
import eu.darken.bb.common.dagger.PerApp

import timber.log.Timber
import javax.inject.Inject

@PerApp
class NOPBugsnagErrorHandler @Inject constructor() : OnErrorCallback {

    override fun onError(event: Event): Boolean {
        Timber.w(event.originalError, "Skipping bugtracking due to user opt-out.")
        return false
    }

}
