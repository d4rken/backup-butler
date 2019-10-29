package eu.darken.bb.common.file.picker

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.file.APath
import eu.darken.bb.common.file.SAFGateway
import eu.darken.bb.common.file.SAFPath
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.Storage
import timber.log.Timber


class APathPickerActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val options: APathPicker.Options,
        private val safGateway: SAFGateway
) : SmartVDC() {

    private val stater = Stater(State())
    val state = stater.liveData

    val launchPickerEvent = SingleLiveEvent<Pair<Intent, APath.Type>>()

    val finishEvent = SingleLiveEvent<APathPicker.Result>()

    init {
//        if (handle.get<Boolean>("used") != true) {
//            handle.set("used", true)
//            if (options.type == null) {
//                showOptions()
//            } else {
//                launchSubPicker(options.type)
//            }
//        }
        if (options.type == null) {
            showOptions()
        } else {
            launchSubPicker(options.type)
        }
    }

    private fun showOptions() {
        stater.update {
            it.copy(showPickList = true, pickTypes = listOf(Storage.Type.LOCAL, Storage.Type.SAF))
        }
    }

    private fun launchSubPicker(type: APath.Type) {
        stater.update {
            it.copy(showPickList = false)
        }
        val intent = when (type) {
            APath.Type.SAF -> safGateway.createPickerIntent()
            else -> TODO()
        }
        launchPickerEvent.postValue(Pair(intent, type))
    }

    fun onSAFPickerResult(data: Uri) {
        Timber.tag(TAG).d("onSSAFPickerResult(data=%s)", data)
        val path = SAFPath.build(data)
        val result = APathPicker.Result(
                options,
                path = path
        )
        finishEvent.postValue(result)
    }

    fun onEmptyResult() {
        Timber.tag(TAG).d("onEmptyResult()")
        if (options.allowTypeChange) {
            showOptions()
        } else {
            finishEvent.postValue(APathPicker.Result(
                    options = options
            ))
        }
    }

    fun onTypeSelected(storageType: Storage.Type) {
        val fileType = when (storageType) {
            Storage.Type.LOCAL -> APath.Type.JAVA
            Storage.Type.SAF -> APath.Type.SAF
        }
        launchSubPicker(fileType)
    }

    data class State(
            val showPickList: Boolean = false,
            val pickTypes: List<Storage.Type> = emptyList()
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<APathPickerActivityVDC> {
        fun create(handle: SavedStateHandle, options: APathPicker.Options): APathPickerActivityVDC
    }

    companion object {
        val TAG = App.logTag("Picker", "Activity", "VDC")
    }
}