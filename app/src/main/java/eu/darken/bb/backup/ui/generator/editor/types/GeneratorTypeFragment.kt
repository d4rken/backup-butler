package eu.darken.bb.backup.ui.generator.editor.types

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.navigation.popBackStack
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.Smart2Fragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.GeneratorEditorTypeselectionFragmentBinding
import javax.inject.Inject

@AndroidEntryPoint
class GeneratorTypeFragment : Smart2Fragment(R.layout.generator_editor_typeselection_fragment) {

    override val vdc: GeneratorTypeFragmentVDC by viewModels()
    override val ui: GeneratorEditorTypeselectionFragmentBinding by viewBinding()
    @Inject lateinit var adapter: GeneratorTypeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.apply {
            recyclerview.setupDefaults(adapter)
            toolbar.apply {
                setNavigationIcon(R.drawable.ic_baseline_close_24)
                setNavigationOnClickListener { popBackStack() }
            }
        }

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.createType(adapter.data[i]) })

        vdc.state.observe2(this) {
            adapter.update(it.supportedTypes)
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
