package eu.darken.bb.task.ui.editor.backup.generators

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.backup.ui.generator.list.GeneratorListAdapter
import eu.darken.bb.backup.ui.generator.picker.GeneratorPickerListener
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.lists.differ.update
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.Smart2Fragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.TaskEditorBackupGeneratorsFragmentBinding
import javax.inject.Inject
import javax.inject.Provider

@AndroidEntryPoint
class GeneratorsFragment : Smart2Fragment(R.layout.task_editor_backup_generators_fragment), GeneratorPickerListener {

    val navArgs by navArgs<GeneratorsFragmentArgs>()
    override val ui: TaskEditorBackupGeneratorsFragmentBinding by viewBinding()
    override val vdc: GeneratorsFragmentVDC by viewModels()

    @Inject lateinit var adapter: GeneratorListAdapter
    @Inject lateinit var pickerAdapterProvider: Provider<GeneratorListAdapter>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupGeneratorPickerListener { vdc.onSourceAdded(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.apply {
            toolbar.setupWithNavController(findNavController())

            listSources.setupDefaults(adapter)

            fab.clicksDebounced().subscribe { vdc.onAddSource() }

            setupbar.buttonPositiveSecondary.clicksDebounced().subscribe { vdc.onNext() }
        }

        vdc.state.observe2(this, ui) { state ->
            log { "Updating UI state with $state" }
            adapter.update(state.sources)
            setupbar.buttonPositiveSecondary.isEnabled = state.sources.isNotEmpty()
        }
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        val TAG = logTag("Backup", "Editor", "Generators", "Fragment")
    }
}
