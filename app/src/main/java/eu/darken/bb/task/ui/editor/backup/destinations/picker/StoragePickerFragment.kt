package eu.darken.bb.task.ui.editor.backup.destinations.picker

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.RecyclerViewWrapperLayout
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.storage.ui.list.StorageAdapter
import javax.inject.Inject

@AndroidEntryPoint
class StoragePickerFragment : SmartFragment() {

    private val vdc: StoragePickerFragmentVDC by viewModels()

    @Inject lateinit var adapter: StorageAdapter
    @BindView(R.id.storage_list_wrapper) lateinit var storageListWrapper: RecyclerViewWrapperLayout
    @BindView(R.id.storage_list) lateinit var storageList: RecyclerView
    @BindView(R.id.fab) lateinit var fab: FloatingActionButton

    init {
        layoutRes = R.layout.task_editor_backup_storages_picker_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        storageList.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.selectStorage(adapter.data[i]) })

        vdc.storageData.observe2(this) { state ->
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

        fab.clicksDebounced().subscribe { vdc.createStorage() }
        super.onViewCreated(view, savedInstanceState)
    }
}
