package eu.darken.bb.storage.ui.viewer.item

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.toastError
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.StorageViewerItemlistFragmentBinding
import eu.darken.bb.processor.ui.ProcessorActivity
import javax.inject.Inject

@AndroidEntryPoint
class StorageItemFragment : SmartFragment(R.layout.storage_viewer_itemlist_fragment) {

    private val vdc: StorageItemFragmentVDC by viewModels()
    private val ui: StorageViewerItemlistFragmentBinding by viewBinding()
    @Inject lateinit var adapter: StorageItemAdapter


    private var showOptionDeleteAll = false

    init {
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.storageItemList.setupDefaults(adapter)

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.viewContent(adapter.data[i]) })

        vdc.state.observe2(this, ui) { state ->
            requireActivityActionBar().title = state.storageLabel
            if (state.storageType != null) {
                requireActivityActionBar().setSubtitle(state.storageType.labelRes)
            }

            adapter.update(state.specInfos)

            storageItemList.setInvisible(state.isWorking)
            loadingOverlay.setInvisible(!state.isWorking)

            showOptionDeleteAll = state.allowDeleteAll && !state.isWorking
            invalidateOptionsMenu()
        }

        vdc.deletionState.observe2(this, ui) { deletionState ->
            if (deletionState.backupSpec != null) {
                loadingOverlay.setPrimaryText(
                    getString(
                        R.string.progress_deleting_x_label,
                        deletionState.backupSpec.getLabel(requireContext())
                    )
                )
            } else {
                loadingOverlay.setPrimaryText(null)
            }
        }

        vdc.contentActionEvent.observe2(this) {
            StorageItemFragmentDirections.actionStorageItemFragmentToStorageItemActionDialog(
                storageId = it.storageId,
                specId = it.backupSpecId,
            ).navigateTo()
        }

        vdc.finishEvent.observe2(this) { finishActivity() }

        vdc.errorEvents.observe2(this) { toastError(it) }

        var snackbar: Snackbar? = null
        vdc.processorEvent.observe2(this) { isActive ->
            if (isVisible && isActive && snackbar == null) {
                snackbar = Snackbar.make(view, R.string.progress_processing_task_label, Snackbar.LENGTH_INDEFINITE)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_storage_viewer_item_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_delete_all).isVisible = showOptionDeleteAll
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_delete_all -> {
            vdc.deleteAll()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
