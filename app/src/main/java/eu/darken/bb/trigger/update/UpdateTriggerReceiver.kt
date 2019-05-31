package eu.darken.bb.trigger.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.android.AndroidInjection
import timber.log.Timber

class UpdateTriggerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Timber.v("onReceive(%s, %s)", context, intent)
        AndroidInjection.inject(this, context)
    }
}
