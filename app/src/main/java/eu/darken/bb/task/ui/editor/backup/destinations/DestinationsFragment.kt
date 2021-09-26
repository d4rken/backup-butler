package eu.darken.bb.task.ui.editor.backup.destinations

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.TaskEditorBackupStoragesFragmentBinding
import eu.darken.bb.storage.ui.list.StorageAdapter
import eu.darken.bb.task.ui.editor.backup.destinations.picker.StoragePickerFragmentArgs
import javax.inject.Inject

@AndroidEntryPoint
class DestinationsFragment : SmartFragment(R.layout.task_editor_backup_storages_fragment) {

    val navArgs by navArgs<DestinationsFragmentArgs>()
    private val ui: TaskEditorBackupStoragesFragmentBinding by viewBinding()
    private val vdc: DestinationsFragmentVDC by viewModels()

    @Inject lateinit var adapter: StorageAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.listDestinations.setupDefaults(adapter)

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.removeDestination(adapter.data[i]) })

        ui.setupbar.buttonPositivePrimary.clicksDebounced().subscribe { vdc.executeTask() }
        ui.setupbar.buttonPositiveSecondary.clicksDebounced().subscribe { vdc.saveTask() }

        vdc.state.observe2(this, ui) { state ->
            adapter.update(state.destinations)

            setupbar.buttonPositivePrimary.isEnabled = state.destinations.isNotEmpty()
            setupbar.buttonPositiveSecondary.isEnabled = state.destinations.isNotEmpty()
        }

        ui.fab.clicksDebounced().subscribe {
            findNavController().navigate(
                R.id.nav_action_show_picker,
                StoragePickerFragmentArgs(taskId = navArgs.taskId).toBundle()
            )
        }

        vdc.finishEvent.observe2(this) { finishActivity() }
        super.onViewCreated(view, savedInstanceState)
    }
}
