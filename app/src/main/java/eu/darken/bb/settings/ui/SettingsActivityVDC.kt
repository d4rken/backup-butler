package eu.darken.bb.settings.ui

import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.Stater
import eu.darken.bb.common.dagger.SavedStateVDCFactory
import eu.darken.bb.task.core.BackupTaskRepo
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.ui.editor.destinations.DestinationsFragment
import eu.darken.bb.task.ui.editor.intro.IntroFragment
import eu.darken.bb.task.ui.editor.sources.SourcesFragment
import kotlin.reflect.KClass


class SettingsActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        private val taskBuilder: TaskBuilder,
        private val taskRepo: BackupTaskRepo
) : SmartVDC() {

    private val stateUpdater = Stater(startValue = State(
            step = State.Step.INTRO,
            allowNext = true
    ))


    init {

    }


    data class State(
            val step: Step,
            val allowPrevious: Boolean = false,
            val allowNext: Boolean = false,
            val saveable: Boolean = false,
            val existingTask: Boolean = false
    ) {
        enum class Step(
                val stepPos: Int,
                val fragmentClass: KClass<out Fragment>
        ) {
            INTRO(0, IntroFragment::class),
            SOURCES(1, SourcesFragment::class),
            DESTINATIONS(2, DestinationsFragment::class)
        }
    }


    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<SettingsActivityVDC>
}