package eu.darken.bb.task.ui.editor.backup.destinations.picker

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.TaskEditorBackupStoragesPickerFragmentBinding
import eu.darken.bb.storage.ui.list.StorageAdapter
import javax.inject.Inject

@AndroidEntryPoint
class StoragePickerFragment : SmartFragment(R.layout.task_editor_backup_storages_picker_fragment) {

    private val vdc: StoragePickerFragmentVDC by viewModels()
    private val ui: TaskEditorBackupStoragesPickerFragmentBinding by viewBinding()

    @Inject lateinit var adapter: StorageAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.storageList.setupDefaults(adapter)

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.selectStorage(adapter.data[i]) })

        vdc.storageData.observe2(this, ui) { state ->
            adapter.update(state.storages)

            if (state.allExistingAdded) {
                storageListWrapper.setEmptyState(
                    R.drawable.ic_emoji_happy,
                    R.string.task_editor_backup_destination_picker_alladded_desc
                )
            } else {
                storageListWrapper.setEmptyState(
                    R.drawable.ic_emoji_neutral,
                    R.string.task_editor_backup_destination_picker_empty_desc
                )
            }

            storageListWrapper.updateLoadingState(state.isLoading)
            fab.setInvisible(state.isLoading)

            requireActivity().invalidateOptionsMenu()
        }

        vdc.finishEvent.observe2(this) {
            findNavController().popBackStack()
        }

        ui.fab.clicksDebounced().subscribe { vdc.createStorage() }
        super.onViewCreated(view, savedInstanceState)
    }
}
