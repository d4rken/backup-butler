package eu.darken.bb.quickmode.ui.files.config

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.takeUntilAfter
import eu.darken.bb.common.navigation.navVia
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.quickmode.core.QuickMode
import eu.darken.bb.quickmode.core.files.FilesQuickMode
import eu.darken.bb.quickmode.ui.apps.config.AppsConfigFragmentVDC
import eu.darken.bb.quickmode.ui.common.config.*
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.picker.StoragePickerResult
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class FilesConfigFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val quickMode: FilesQuickMode,
    private val storageManager: StorageManager,
    private val dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {

    private val configHD = quickMode.state


    private val storageItemFlow: Flow<ConfigAdapter.Item> = configHD.flow
        .flatMapLatest { data ->
            storageManager.infos(data.storageIds)
                .takeUntilAfter { infos ->
                    infos.all { it.isFinished }
                }
        }
        .map { storageInfos ->
            when (storageInfos.size) {
                0 -> StorageCreateVH.Item(
                    onSetupStorage = {
                        FilesConfigFragmentDirections.actionFilesConfigFragmentToStoragePicker().navVia(this)
                    }
                )
                1 -> StorageInfoVH.Item(
                    infoOpt = storageInfos.single(),
                    onRemove = { toRemove ->
                        configHD.updateAsync {
                            copy(storageIds = storageIds.minus(toRemove))
                        }
                    },
                )
                else -> StorageErrorMultipleVH.Item
            }
        }

    val state: LiveData<AppsConfigFragmentVDC.State> = combine(configHD.flow, storageItemFlow) { config, storageItem ->
        val items = mutableListOf<ConfigAdapter.Item>()

        if (config.storageIds.isEmpty()) {
            AutoSetupVH.Item(
                onAutoSetup = { runAutoSetUp() }
            )
//                    .run { items.add(this) }
        }

        items.add(storageItem)

        FilesOptionVH.Item(
            replaceExisting = false,
            replaceExistingOnToggle = {

            },
        ).run { items.add(this) }


        AppsConfigFragmentVDC.State(
            items = items,
            isExisting = config.storageIds.isNotEmpty()
        )
    }
        .catch {
            errorEvents.postValue(it)
            emit(AppsConfigFragmentVDC.State())
        }
        .asLiveData2()

    private fun runAutoSetUp() {
        log(TAG) { "runAutoSetUp()" }
    }

    fun onStoragePickerResult(result: StoragePickerResult?) = launch {
        log(TAG) { "onStoragePickerResult(result=$result)" }
        if (result == null) return@launch

        configHD.updateBlocking {
            copy(storageIds = storageIds.plus(result.storageId))
        }
    }

    fun reset() = launch {
        log(TAG) { "reset()" }
        quickMode.reset(QuickMode.Type.FILES)
    }

    data class State(
        val items: List<ConfigAdapter.Item> = emptyList(),
        val isExisting: Boolean = false,
    )

    companion object {
        private val TAG = logTag("QuickMode", "Files", "Wizard", "VDC")
    }
}