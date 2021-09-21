package eu.darken.bb.storage.ui.list

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
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
import eu.darken.bb.common.ui.RecyclerViewWrapperLayout
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.processor.ui.ProcessorActivity
import javax.inject.Inject

@AndroidEntryPoint
class StorageListFragment : SmartFragment(R.layout.storage_list_fragment) {

    private val vdc: StorageListFragmentVDC by viewModels()

    @Inject lateinit var adapter: StorageAdapter
    @BindView(R.id.storage_list) lateinit var storageList: RecyclerView
    @BindView(R.id.storage_list_wrapper) lateinit var storageListWrapper: RecyclerViewWrapperLayout
    @BindView(R.id.fab) lateinit var fab: FloatingActionButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        storageList.setupDefaults(adapter)

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.editStorage(adapter.data[i]) })

        vdc.storageData.observe2(this) { state ->
            adapter.update(state.storages)

            storageListWrapper.updateLoadingState(state.isLoading)

            fab.setInvisible(state.isLoading)

            requireActivity().invalidateOptionsMenu()
        }

        fab.clicksDebounced().subscribe { vdc.createStorage() }

        vdc.editTaskEvent.observe2(this) {
            doNavigate(StorageListFragmentDirections.actionStorageListFragmentToStorageActionDialog(it))
        }

        var snackbar: Snackbar? = null
        vdc.processorEvent.observe2(this) { isActive ->
            if (isVisible && isActive && snackbar == null) {
                snackbar = Snackbar.make(view, R.string.progress_processing_task_label, Snackbar.LENGTH_INDEFINITE)
                    .setAnchorView(fab)
                    .setAction(R.string.general_show_action) {
                        startActivity(Intent(requireContext(), ProcessorActivity::class.java))
                    }
                snackbar?.show()
            } else if (!isActive && snackbar != null) {
                snackbar?.dismiss()
                snackbar = null
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }


}
