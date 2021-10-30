package eu.darken.bb.common.navigation

import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavDirections

fun NavDirections.via(pub: MutableLiveData<in NavDirections>) = pub.postValue(this)

fun NavDirections.via(provider: NavEventsSource) = this.via(provider.navEvents)

interface NavEventsSource {
    val navEvents: MutableLiveData<in NavDirections>
}