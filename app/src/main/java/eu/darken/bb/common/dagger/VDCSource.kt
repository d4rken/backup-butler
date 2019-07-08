package eu.darken.bb.common.dagger

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.VDC

class VDCSource @AssistedInject constructor(
        private val creators: @JvmSuppressWildcards Map<Class<out VDC>, SavedStateVDCFactory<out VDC>>,
        @Assisted savedStateOwner: SavedStateRegistryOwner,
        @Assisted defaultSavedState: Bundle?
) : AbstractSavedStateViewModelFactory(savedStateOwner, defaultSavedState) {

    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        val factory = creators.entries.find { modelClass.isAssignableFrom(it.key) }?.value

        @Suppress("UNCHECKED_CAST")
        return factory?.create(handle) as? T ?: throw IllegalStateException("Unknown VDC factory: $modelClass")
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(savedStateOwner: SavedStateRegistryOwner, defaultSavedState: Bundle?): ViewModelProvider.Factory
    }
}