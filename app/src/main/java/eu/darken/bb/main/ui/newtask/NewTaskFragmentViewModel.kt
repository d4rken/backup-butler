package eu.darken.bb.main.ui.newtask

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SmartViewModel
import eu.darken.bb.common.dagger.SavedStateVDCFactory
import eu.darken.bb.upgrades.UpgradeControl
import eu.darken.bb.upgrades.UpgradeData
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables

class NewTaskFragmentViewModel @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        private val upgradeControl: UpgradeControl
) : SmartViewModel() {

    val appState: LiveData<State> = Observables
            .zip(Observable.empty<String>(), upgradeControl.upgradeData)
            .map { (appInfo, upgradeData) ->
                return@map State(
                        upgradeData = upgradeData
                )
            }
            .toLiveData()

    init {

    }

    fun test() {

    }

    data class State(
            val upgradeData: UpgradeData
    )

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<NewTaskFragmentViewModel>
}