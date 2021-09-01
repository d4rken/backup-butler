package eu.darken.bb.common.files.ui.picker

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.saf.SAFGateway
import eu.darken.bb.common.files.core.saf.SAFPath
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.Storage
import timber.log.Timber


class APathPickerActivityVDC @AssistedInject constructor(
    @Assisted private val handle: SavedStateHandle,
    @Assisted private val options: APathPicker.Options,
    val safGateway: SAFGateway
) : SmartVDC() {

    val launchSAFEvents = SingleLiveEvent<Intent>()
    val launchLocalEvents = SingleLiveEvent<APathPicker.Options>()
    val launchTypesEvents = SingleLiveEvent<APathPicker.Options>()

    val resultEvents = SingleLiveEvent<Pair<APathPicker.Result, Boolean>>()

    init {
        // Default result is canceled
        resultEvents.postValue(APathPicker.Result(options = options) to false)

        val startType = when {
            options.startPath != null -> options.startPath.pathType
            options.allowedTypes.size == 1 -> options.allowedTypes.single()
            else -> null
        }
        when (startType) {
            APath.PathType.RAW -> throw UnsupportedOperationException("$startType is not supported")
            APath.PathType.LOCAL -> {
                showLocal()
            }
            APath.PathType.SAF -> {
                showSAF()
            }
            null -> {
                showPicker()
            }
        }

    }

    private fun showLocal() {
        launchLocalEvents.postValue(options)
    }

    private fun showSAF() {
        launchSAFEvents.postValue(safGateway.createPickerIntent())
    }

    private fun showPicker() {
        launchTypesEvents.postValue(options)
    }

    fun onSAFPickerResult(data: Uri?) {
        Timber.tag(TAG).d("onSSAFPickerResult(data=%s)", data)
        if (data != null) {
            val path = SAFPath.build(data)
            val takenPermissions = mutableSetOf<SAFPath>()
            if (!safGateway.hasPermission(path) && safGateway.takePermission(path)) {
                takenPermissions.add(path)
            }
            val result = APathPicker.Result(
                options,
                selection = setOf(path),
                persistedPermissions = takenPermissions
            )
            resultEvents.postValue(result to true)
        } else {
            if (options.allowedTypes.size > 1) {
                showPicker()
            } else {
                resultEvents.postValue(APathPicker.Result(options = options) to true)
            }
        }
    }

    fun onTypePicked(type: Storage.Type) {
        when (type) {
            Storage.Type.LOCAL -> showLocal()
            Storage.Type.SAF -> showSAF()
        }
    }

    fun onResult(result: APathPicker.Result) {
        resultEvents.postValue(result to true)
    }

    @AssistedFactory
    interface Factory : VDCFactory<APathPickerActivityVDC> {
        fun create(handle: SavedStateHandle, options: APathPicker.Options): APathPickerActivityVDC
    }

    companion object {
        val TAG = App.logTag("Picker", "Activity", "VDC")
    }
}