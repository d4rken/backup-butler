package eu.darken.bb.tasks.ui.editor.sources

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.tasks.core.TaskBuilder
import java.util.*

class SourcesFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: UUID,
        private val taskBuilder: TaskBuilder
) : VDC() {

    val state = MutableLiveData<State>(State(emoji = ""))

    data class State(val emoji: String)


    @AssistedInject.Factory
    interface Factory : VDCFactory<SourcesFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: UUID): SourcesFragmentVDC
    }
}