package eu.darken.bb.common.debug.bugsnag


import com.bugsnag.android.BeforeNotify
import eu.darken.bb.common.dagger.PerApp

import timber.log.Timber
import javax.inject.Inject

@PerApp
class NOPBugsnagErrorHandler @Inject constructor(

) : BeforeNotify {
    override fun run(error: com.bugsnag.android.Error): Boolean {
        Timber.w(error.exception, "Skipping bugtracking due to user opt-out.")
        return false
    }
}
