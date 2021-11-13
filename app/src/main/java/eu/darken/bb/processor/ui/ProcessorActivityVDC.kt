package eu.darken.bb.processor.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.smart.SmartVDC
import javax.inject.Inject

@HiltViewModel
class ProcessorActivityVDC @Inject constructor(
    private val handle: SavedStateHandle
) : SmartVDC()