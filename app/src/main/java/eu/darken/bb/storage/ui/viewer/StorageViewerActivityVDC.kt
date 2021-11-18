package eu.darken.bb.storage.ui.viewer

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class StorageViewerActivityVDC @Inject constructor(
    handle: SavedStateHandle,
    storageManager: StorageManager,
    private val dispatcherProvider: DispatcherProvider
) : Smart2VDC(dispatcherProvider) {
    private val navArgs = handle.navArgs<StorageViewerActivityArgs>()
    private val storageId = navArgs.value.storageId

    val finishActivity = SingleLiveEvent<Boolean>()

    private val stater: Stater<State> = Stater { State(storageId = storageId) }
    val state = stater.liveData

    init {
        storageManager.infos(listOf(storageId))
            .map { it.single() }
            .onEach { optInfo ->
                stater.update {
                    it.copy(
                        storageId = storageId,
                        storageType = optInfo.info?.storageType,
                        label = optInfo.info?.config?.label ?: ""
                    )
                }
                if (optInfo.anyError != null) {
                    errorEvents.postValue(optInfo.anyError)
                    finishActivity.postValue(true)
                }
            }
            .launchInViewModel()
    }

    data class State(
        val storageId: Storage.Id,
        val label: String = "",
        val storageType: Storage.Type? = null,
        val loading: Boolean = true
    )
}