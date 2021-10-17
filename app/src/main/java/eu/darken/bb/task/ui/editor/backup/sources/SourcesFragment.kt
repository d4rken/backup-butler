package eu.darken.bb.task.ui.editor.backup.sources

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.backup.ui.generator.list.GeneratorAdapter
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.TaskEditorBackupGeneratorsFragmentBinding
import eu.darken.bb.storage.ui.picker.StoragePickerFragmentArgs
import eu.darken.bb.task.ui.editor.backup.destinations.DestinationsFragmentArgs
import javax.inject.Inject
import javax.inject.Provider

@AndroidEntryPoint
class SourcesFragment : SmartFragment(R.layout.task_editor_backup_generators_fragment) {

    val navArgs by navArgs<SourcesFragmentArgs>()
    private val ui: TaskEditorBackupGeneratorsFragmentBinding by viewBinding()
    private val vdc: SourcesFragmentVDC by viewModels()

    @Inject lateinit var adapter: GeneratorAdapter
    @Inject lateinit var pickerAdapterProvider: Provider<GeneratorAdapter>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.listSources.setupDefaults(adapter)

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.removeSource(adapter.data[i]) })

        vdc.state.observe2(this, ui) { state ->
            log { "Updating UI state with $state" }
            adapter.update(state.sources)
            ui.setupbar.buttonPositiveSecondary.isEnabled = state.sources.isNotEmpty()
        }

        ui.fab.clicksDebounced().subscribe {
            findNavController().navigate(
                R.id.nav_action_show_picker,
                StoragePickerFragmentArgs(taskId = navArgs.taskId).toBundle()
            )
        }

        ui.setupbar.buttonPositiveSecondary.clicksDebounced().subscribe {
            findNavController().navigate(
                R.id.nav_action_next,
                DestinationsFragmentArgs(taskId = navArgs.taskId).toBundle()
            )
        }
        super.onViewCreated(view, savedInstanceState)
    }
}
