package eu.darken.bb.common.navigation

import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavDirections
import eu.darken.bb.common.SingleLiveEvent

fun NavDirections.via(pub: MutableLiveData<in NavDirections>) = pub.postValue(this)

fun NavDirections.via(provider: NavEventsSource) = this.via(provider.navEvents)

interface NavEventsSource {
    val navEvents: SingleLiveEvent<in NavDirections>
}