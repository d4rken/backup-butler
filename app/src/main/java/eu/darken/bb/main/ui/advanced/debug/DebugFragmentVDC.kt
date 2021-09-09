package eu.darken.bb.main.ui.advanced.debug

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.vdc.SmartVDC
import javax.inject.Inject

@HiltViewModel
class DebugFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : SmartVDC()