package eu.darken.bb.backup.ui.generator.editor.types

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.GeneratorEditorTypeselectionFragmentBinding
import javax.inject.Inject

@AndroidEntryPoint
class GeneratorTypeFragment : SmartFragment(R.layout.generator_editor_typeselection_fragment) {

    val navArgs by navArgs<GeneratorTypeFragmentArgs>()

    private val vdc: GeneratorTypeFragmentVDC by viewModels()
    private val ui: GeneratorEditorTypeselectionFragmentBinding by viewBinding()
    @Inject lateinit var adapter: GeneratorTypeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.recyclerview.setupDefaults(adapter)

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.createType(adapter.data[i]) })

        vdc.state.observe2(this) {
            adapter.update(it.supportedTypes)
        }

        vdc.navigationEvent.observe2(this) { (type, id) ->
            val nextStep = when (type) {
                Backup.Type.APP -> GeneratorTypeFragmentDirections.actionGeneratorTypeFragmentToAppEditorFragment(id)
                Backup.Type.FILES -> GeneratorTypeFragmentDirections.actionGeneratorTypeFragmentToFilesEditorFragment(id)
            }
            doNavigate(nextStep)
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
