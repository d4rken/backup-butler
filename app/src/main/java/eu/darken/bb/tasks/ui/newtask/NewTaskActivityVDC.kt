package eu.darken.bb.tasks.ui.newtask

import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.StateUpdater
import eu.darken.bb.common.dagger.SavedStateVDCFactory
import eu.darken.bb.tasks.ui.newtask.destinations.DestinationsFragment
import eu.darken.bb.tasks.ui.newtask.intro.IntroFragment
import eu.darken.bb.tasks.ui.newtask.sources.SourcesFragment
import kotlin.reflect.KClass


class NewTaskActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle
) : SmartVDC() {

    private val stateUpdater = StateUpdater(startValue = State(
            step = State.Step.INTRO,
            allowNext = true
    ))
    val state = stateUpdater.state

    fun changeStep(dir: Int) {
        stateUpdater.update { old ->
            val new = State.Step.values().find { it.stepPos == old.step.stepPos + dir }
            val allowNext = State.Step.values().find { new != null && it.stepPos == new.stepPos + 1 } != null
            val allowPrevious = State.Step.values().find { new != null && it.stepPos == new.stepPos - 1 } != null
            return@update old.copy(
                    step = new ?: old.step,
                    allowNext = allowNext,
                    allowPrevious = allowPrevious
            )
        }
    }

    data class State(
            val step: Step,
            val allowPrevious: Boolean = false,
            val allowNext: Boolean = false
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
    interface Factory : SavedStateVDCFactory<NewTaskActivityVDC>
}