package eu.darken.bb.backup.ui.generator.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.jakewharton.rxbinding4.view.clicks
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.GeneratorListFragmentBinding
import eu.darken.bb.main.ui.advanced.AdvancedModeFragmentDirections
import javax.inject.Inject

@AndroidEntryPoint
class GeneratorListFragment : SmartFragment(R.layout.generator_list_fragment) {

    private val vdc: GeneratorsFragmentVDC by viewModels()
    private val binding: GeneratorListFragmentBinding by viewBinding()
    @Inject lateinit var adapter: GeneratorAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.generatorList.setupDefaults(adapter)

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.editGenerator(adapter.data[i]) })

        vdc.viewState.observe2(this) {
            log { "Updating UI state with $it" }
            adapter.update(it.generators)
        }

        binding.fab.clicks().subscribe { vdc.newGenerator() }

        vdc.editTaskEvent.observe2(this) {
            doNavigate(AdvancedModeFragmentDirections.actionAdvancedModeFragmentToGeneratorsActionDialog(it.generatorId))
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
