package eu.darken.bb.storage.ui.list

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.lists.differ.update
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.Smart2Fragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.StorageListFragmentBinding
import eu.darken.bb.processor.ui.ProcessorActivity
import javax.inject.Inject

@AndroidEntryPoint
class StorageListFragment : Smart2Fragment(R.layout.storage_list_fragment) {

    override val vdc: StorageListFragmentVDC by viewModels()
    override val ui: StorageListFragmentBinding by viewBinding()

    @Inject lateinit var adapter: StorageAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.storageList.setupDefaults(adapter)

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.editStorage(adapter.data[i]) })

        vdc.storageData.observe2(this, ui) { state ->
            log { "Updating UI state with $state" }
            adapter.update(state.storages)
            fab.isInvisible = state.isLoading
            storageListWrapper.updateLoadingState(state.isLoading)
            requireActivity().invalidateOptionsMenu()
        }

        ui.fab.clicksDebounced().subscribe { vdc.createStorage() }

        var snackbar: Snackbar? = null
        vdc.processorEvent.observe2(this) { isActive ->
            if (isVisible && isActive && snackbar == null) {
                snackbar = Snackbar.make(view, R.string.progress_processing_task_label, Snackbar.LENGTH_INDEFINITE)
                    .setAnchorView(ui.fab)
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
