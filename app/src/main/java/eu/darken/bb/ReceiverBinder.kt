package eu.darken.bb

import dagger.Module
import dagger.android.ContributesAndroidInjector
import eu.darken.bb.trigger.update.UpdateTriggerReceiver

@Module
internal abstract class ReceiverBinder {
    @ContributesAndroidInjector
    internal abstract fun updateTrigger(): UpdateTriggerReceiver

}