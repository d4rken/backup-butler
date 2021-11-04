package eu.darken.bb.backup.ui.generator.picker

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.backup.ui.generator.list.GeneratorListAdapter
import eu.darken.bb.common.lists.differ.update
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.navigation.popBackStack
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.GeneratorPickerFragmentBinding
import javax.inject.Inject

@AndroidEntryPoint
class GeneratorPickerFragment : SmartFragment(R.layout.generator_picker_fragment) {

    private val vdc: GeneratorPickerFragmentVDC by viewModels()
    private val ui: GeneratorPickerFragmentBinding by viewBinding()

    @Inject lateinit var adapter: GeneratorListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.apply {
            generatorList.setupDefaults(adapter)
            toolbar.setNavigationOnClickListener { popBackStack() }
            fab.clicksDebounced().subscribe { vdc.createGenerator() }
        }

        vdc.generatorData.observe2(this, ui) { state ->
            adapter.update(state.items)

            if (state.allExistingAdded) {
                generatorListWrapper.setEmptyState(
                    R.drawable.ic_emoji_happy,
                    R.string.generator_picker_alladded_desc
                )
            } else {
                generatorListWrapper.setEmptyState(
                    R.drawable.ic_emoji_neutral,
                    R.string.generator_picker_empty_desc
                )
            }

            generatorListWrapper.updateLoadingState(state.isLoading)
            fab.isInvisible = state.isLoading
        }

        vdc.navEvents.observe2(this) { doNavigate(it) }

        vdc.finishEvent.observe2(this) {
            setGeneratorPickerResult(it)
            popBackStack()
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
