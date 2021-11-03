package eu.darken.bb.quickmode.ui.files.config

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.navigation.NavEventsSource
import eu.darken.bb.common.navigation.via
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.quickmode.core.FilesQuickMode
import eu.darken.bb.quickmode.core.QuickMode
import eu.darken.bb.quickmode.ui.apps.config.AppsConfigFragmentVDC
import eu.darken.bb.quickmode.ui.common.config.*
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.picker.StoragePickerResult
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

@HiltViewModel
class FilesConfigFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val quickMode: FilesQuickMode,
    private val storageManager: StorageManager,
) : SmartVDC(), NavEventsSource {

    private val configHD = quickMode.hotData

    override val navEvents = SingleLiveEvent<NavDirections?>()
    val errorEvent = SingleLiveEvent<Throwable>()

    private val storageItemObs: Observable<ConfigAdapter.Item> = configHD.data
        .switchMap { data ->
            storageManager.infos(data.storageIds).takeUntil { infos ->
                infos.all { it.isFinished }
            }
        }
        .map { storageInfos ->
            when (storageInfos.size) {
                0 -> StorageCreateVH.Item(
                    onSetupStorage = {
                        FilesConfigFragmentDirections.actionFilesConfigFragmentToStoragePicker().via(this)
                    }
                )
                1 -> StorageInfoVH.Item(
                    infoOpt = storageInfos.single(),
                    onRemove = { toRemove ->
                        configHD.update {
                            it.copy(storageIds = it.storageIds.minus(toRemove))
                        }
                    },
                )
                else -> StorageErrorMultipleVH.Item
            }
        }

    val state: LiveData<AppsConfigFragmentVDC.State> = Observable
        .combineLatest(configHD.data, storageItemObs) { config, storageItem ->
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
        .doOnError { errorEvent.postValue(it) }
        .onErrorReturnItem(AppsConfigFragmentVDC.State())
        .asLiveData()

    private fun runAutoSetUp() {
        log(TAG) { "runAutoSetUp()" }
    }

    fun onStoragePickerResult(result: StoragePickerResult?) {
        log(TAG) { "onStoragePickerResult(result=$result)" }
        if (result == null) return

        configHD.update {
            it.copy(storageIds = it.storageIds.plus(result.storageId))
        }
    }

    fun reset() {
        log(TAG) { "reset()" }
        quickMode.reset(QuickMode.Type.FILES).subscribe { _ ->

        }
    }

    data class State(
        val items: List<ConfigAdapter.Item> = emptyList(),
        val isExisting: Boolean = false,
    )

    companion object {
        private val TAG = logTag("QuickMode", "Files", "Wizard", "VDC")
    }
}