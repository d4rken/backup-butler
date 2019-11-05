package eu.darken.bb.task.ui.editor.backup.destinations.picker

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.LoadingOverlayView
import eu.darken.bb.common.ui.RecyclerViewWrapperLayout
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.storage.ui.list.StorageAdapter
import javax.inject.Inject


class StoragePickerFragment : SmartFragment(), AutoInject {

    val navArgs by navArgs<StoragePickerFragmentArgs>()

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: StoragePickerFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as StoragePickerFragmentVDC.Factory
        factory.create(handle, navArgs.taskId)
    })

    @Inject lateinit var adapter: StorageAdapter
    @BindView(R.id.storage_list_wrapper) lateinit var storageListWrapper: RecyclerViewWrapperLayout
    @BindView(R.id.storage_list) lateinit var storageList: RecyclerView
    @BindView(R.id.fab) lateinit var fab: FloatingActionButton
    @BindView(R.id.loading_overlay) lateinit var loadingOverlay: LoadingOverlayView

    init {
        layoutRes = R.layout.task_editor_backup_storages_picker_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        storageList.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.selectStorage(adapter.data[i]) })

        vdc.storageData.observe2(this) { state ->
            adapter.update(state.storages)

            if (state.allExistingAdded) {
                storageListWrapper.setEmptyInfos(R.drawable.ic_emoji_happy, R.string.task_editor_backup_destination_picker_alladded_desc)
            } else {
                storageListWrapper.setEmptyInfos(R.drawable.ic_emoji_neutral, R.string.task_editor_backup_destination_picker_empty_desc)
            }

            loadingOverlay.setInvisible(!state.isLoading)
            storageListWrapper.setInvisible(state.isLoading)
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
