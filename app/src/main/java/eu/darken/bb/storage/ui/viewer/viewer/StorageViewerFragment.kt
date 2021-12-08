package eu.darken.bb.storage.ui.viewer.viewer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.Smart2Fragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.StorageViewerViewerFragmentBinding
import eu.darken.bb.processor.ui.ProcessorActivity
import javax.inject.Inject

@AndroidEntryPoint
class StorageViewerFragment : Smart2Fragment(R.layout.storage_viewer_viewer_fragment) {

    override val vdc: StorageViewerFragmentVDC by viewModels()
    override val ui: StorageViewerViewerFragmentBinding by viewBinding()
    @Inject lateinit var adapter: StorageViewerAdapter


    private var showOptionDeleteAll = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.apply {
            storageList.setupDefaults(adapter)
        }

        ui.toolbar.apply {
            setupWithNavController(findNavController())

            setOnCreateContextMenuListener { menu, v, menuInfo ->
                menu.findItem(R.id.action_delete_all).isVisible = showOptionDeleteAll
            }
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_delete_all -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setMessage(R.string.general_delete_all_action)
                            .setPositiveButton(R.string.general_delete_all_action) { _, _ ->
                                vdc.deleteAll()
                            }
                            .setNegativeButton(R.string.general_cancel_action) { _, _ -> }
                            .show()
                        true
                    }
                    else -> super.onOptionsItemSelected(it)
                }
            }
        }

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.viewContent(adapter.data[i]) })

        vdc.state.observe2(this, ui) { state ->
            toolbar.apply {
                if (state.storageType != null) {
                    title = getString(state.storageType.labelRes)
                }
                subtitle = state.storageLabel
            }

            adapter.update(state.specInfos)

            storageListWrapper.updateLoadingState(state.isWorking)

            showOptionDeleteAll = state.allowDeleteAll && !state.isWorking
            invalidateOptionsMenu()
        }

        vdc.deletionState.observe2(this, ui) { deletionState ->
            if (deletionState.backupSpec != null) {
                storageListWrapper.setLoadingText(
                    getString(
                        R.string.progress_deleting_x_label,
                        deletionState.backupSpec.getLabel(requireContext())
                    )
                )
            } else {
                storageListWrapper.setLoadingText(null)
            }
        }

        vdc.contentActionEvent.observe2(this) {
            StorageViewerFragmentDirections.actionStorageItemFragmentToStorageItemActionDialog(
                storageId = it.storageId,
                specId = it.backupSpecId,
            ).run { doNavigate(this) }
        }

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

}
