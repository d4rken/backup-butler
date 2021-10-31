package eu.darken.bb.storage.ui.picker

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.lists.differ.update
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.navigation.popBackStack
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.StoragePickerFragmentBinding
import eu.darken.bb.storage.ui.editor.StorageEditorResultListener
import eu.darken.bb.storage.ui.list.StorageAdapter
import javax.inject.Inject

@AndroidEntryPoint
class StoragePickerFragment : SmartFragment(R.layout.storage_picker_fragment),
    StorageEditorResultListener {

    private val vdc: StoragePickerFragmentVDC by viewModels()
    private val ui: StoragePickerFragmentBinding by viewBinding()

    @Inject lateinit var adapter: StorageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupStorageEditorListener {
            log { "storageEditorListener: $it" }
            vdc.onStorageEditorResult(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.apply {
            storageList.setupDefaults(adapter)
            toolbar.apply {
                setupWithNavController(findNavController())
                setNavigationIcon(R.drawable.ic_baseline_close_24)
            }
            fab.clicksDebounced().subscribe { vdc.createStorage() }
        }

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
            fab.isInvisible = state.isLoading

            requireActivity().invalidateOptionsMenu()
        }

        vdc.navEvents.observe2(this) { doNavigate(it) }

        vdc.finishEvent.observe2(this) {
            setStoragePickerResult(it)
            popBackStack()
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
