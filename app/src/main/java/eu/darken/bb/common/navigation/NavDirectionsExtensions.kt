package eu.darken.bb.common.navigation

import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavDirections

fun NavDirections.via(pub: MutableLiveData<NavDirections>) = pub.postValue(this)

fun NavDirections.via(provider: NavDirectionsProvider) = this.via(provider.navEvents)

interface NavDirectionsProvider {
    val navEvents: MutableLiveData<NavDirections>
}