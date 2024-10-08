package eu.darken.bb.task.ui.editor.backup.storages

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.differ.update
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.smart.Smart2Fragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.TaskEditorBackupStoragesFragmentBinding
import eu.darken.bb.storage.ui.list.StorageAdapter
import eu.darken.bb.storage.ui.picker.StoragePickerListener
import javax.inject.Inject

@AndroidEntryPoint
class StoragesFragment : Smart2Fragment(R.layout.task_editor_backup_storages_fragment), StoragePickerListener {

    val navArgs by navArgs<StoragesFragmentArgs>()
    override val ui: TaskEditorBackupStoragesFragmentBinding by viewBinding()
    override val vdc: StoragesFragmentVDC by viewModels()

    @Inject lateinit var adapter: StorageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupStoragePickerListener { vdc.onStoragePicked(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.apply {
            toolbar.setupWithNavController(findNavController())

            setupbar.apply {
                buttonPositiveSecondary.setOnClickListener { vdc.onNext() }
            }
            fab.setOnClickListener { vdc.addStorage() }

            listDestinations.setupDefaults(adapter)
        }

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.removeDestination(adapter.data[i]) })

        vdc.state.observe2(ui) { state ->
            adapter.update(state.destinations)

            setupbar.buttonPositiveSecondary.isEnabled = state.destinations.isNotEmpty()
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
