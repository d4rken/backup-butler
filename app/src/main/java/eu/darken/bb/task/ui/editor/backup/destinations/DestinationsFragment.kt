package eu.darken.bb.task.ui.editor.backup.destinations

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.TaskEditorBackupStoragesFragmentBinding
import eu.darken.bb.storage.ui.list.StorageAdapter
import eu.darken.bb.storage.ui.picker.StoragePickerResultListener
import javax.inject.Inject

@AndroidEntryPoint
class DestinationsFragment : SmartFragment(R.layout.task_editor_backup_storages_fragment), StoragePickerResultListener {

    val navArgs by navArgs<DestinationsFragmentArgs>()
    private val ui: TaskEditorBackupStoragesFragmentBinding by viewBinding()
    private val vdc: DestinationsFragmentVDC by viewModels()

    @Inject lateinit var adapter: StorageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupStoragePickerListener { vdc.onStoragePicked(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.apply {
            toolbar.setupWithNavController(findNavController())

            setupbar.apply {
                buttonPositivePrimary.clicksDebounced().subscribe { vdc.executeTask() }
                buttonPositiveSecondary.clicksDebounced().subscribe { vdc.saveTask() }
            }
            fab.clicksDebounced().subscribe { vdc.addStorage() }

            listDestinations.setupDefaults(adapter)
        }

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.removeDestination(adapter.data[i]) })

        vdc.state.observe2(this, ui) { state ->
            adapter.update(state.destinations)

            setupbar.buttonPositivePrimary.isEnabled = state.destinations.isNotEmpty()
            setupbar.buttonPositiveSecondary.isEnabled = state.destinations.isNotEmpty()
        }

        vdc.navEvents.observe2(this) { doNavigate(it) }
        vdc.finishEvent.observe2(this) {
            findNavController().popBackStack(R.id.mainFragment, false)
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
