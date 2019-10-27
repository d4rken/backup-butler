package eu.darken.bb.common.file.picker

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.file.APath
import eu.darken.bb.common.file.SAFGateway
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory


class APathPickerActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val options: APathPicker.Options,
        private val safGateway: SAFGateway
) : SmartVDC() {

    private val stater = Stater(State(options = options))
    val state = stater.liveData

    val launchPicker = SingleLiveEvent<Intent>()
    val showOptions = SingleLiveEvent<Any>()

    init {
        val intent = when (options.type) {
            APath.Type.SAF -> safGateway.createPickerIntent()
            null -> null
            else -> TODO()
        }
        if (intent == null) {
            showOptions.postValue(Any())
        } else {
            launchPicker.postValue(APathPicker.intoIntent(intent, options))
        }
    }

    fun onPickerResult() {
        TODO("not implemented")
    }

    data class State(
            val options: APathPicker.Options
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<APathPickerActivityVDC> {
        fun create(handle: SavedStateHandle, options: APathPicker.Options): APathPickerActivityVDC
    }
}