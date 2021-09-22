package eu.darken.bb.backup.ui.generator.editor.types

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.ui.generator.editor.types.app.config.AppEditorConfigFragmentArgs
import eu.darken.bb.backup.ui.generator.editor.types.files.FilesEditorConfigFragmentArgs
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.GeneratorEditorTypeselectionFragmentBinding
import javax.inject.Inject

@AndroidEntryPoint
class GeneratorTypeFragment : SmartFragment(R.layout.generator_editor_typeselection_fragment) {

    val navArgs by navArgs<GeneratorTypeFragmentArgs>()

    private val vdc: GeneratorTypeFragmentVDC by viewModels()
    private val binding: GeneratorEditorTypeselectionFragmentBinding by viewBinding()
    @Inject lateinit var adapter: GeneratorTypeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerview.setupDefaults(adapter)

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.createType(adapter.data[i]) })

        vdc.state.observe2(this) {
            adapter.update(it.supportedTypes)
        }

        vdc.navigationEvent.observe2(this) { (type, id) ->
            val nextStep = when (type) {
                Backup.Type.APP -> R.id.action_generatorTypeFragment_to_appEditorFragment
                Backup.Type.FILES -> R.id.action_generatorTypeFragment_to_filesEditorFragment
            }
            val args = when (type) {
                Backup.Type.APP -> AppEditorConfigFragmentArgs(generatorId = id).toBundle()
                Backup.Type.FILES -> FilesEditorConfigFragmentArgs(generatorId = id).toBundle()
            }
            val appbarConfig =
                AppBarConfiguration.Builder(R.id.appEditorConfigFragment, R.id.filesEditorFragment).build()
            setupActionBarWithNavController(requireActivity() as AppCompatActivity, findNavController(), appbarConfig)
            findNavController().navigate(nextStep, args)
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
