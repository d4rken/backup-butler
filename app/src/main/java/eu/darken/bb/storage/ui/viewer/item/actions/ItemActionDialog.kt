package eu.darken.bb.storage.ui.viewer.item.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.differ.update
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.Smart2BottomSheetDialogFragment
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.databinding.StorageViewerContentlistActionDialogBinding
import eu.darken.bb.storage.ui.viewer.StorageViewerActivity
import eu.darken.bb.storage.ui.viewer.content.ItemContentsFragmentArgs
import eu.darken.bb.task.core.TaskRepo
import javax.inject.Inject

@AndroidEntryPoint
class ItemActionDialog : Smart2BottomSheetDialogFragment() {

    override val vdc: ItemActionDialogVDC by viewModels()
    private val activityVdc by lazy { (requireActivity() as StorageViewerActivity).vdc }
    override lateinit var ui: StorageViewerContentlistActionDialogBinding

    @Inject lateinit var taskRepo: TaskRepo
    @Inject lateinit var actionsAdapter: ActionsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ui = StorageViewerContentlistActionDialogBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.actionlist.setupDefaults(actionsAdapter)

        vdc.state.observe2(this) { state ->
            if (state.info != null) {
                ui.typeLabel.text = getString(state.info.backupSpec.backupType.labelRes)
                ui.storageLabel.text = state.info.backupSpec.getLabel(requireContext())
            } else {
                ui.typeLabel.setText(R.string.general_unknown_label)
                ui.storageLabel.setText(R.string.progress_loading_label)
            }
            ui.infoContainerLoading.setInvisible(state.info != null)

            actionsAdapter.update(state.allowedActions)

            ui.actionlist.setInvisible(state.isWorking)
            ui.actionlistLoading.setInvisible(!state.isWorking)
            if (state.currentOp != null) {
                ui.actionlistLoading.setPrimaryText(state.currentOp.label.get(requireContext()))
            }
        }

        vdc.actionEvent.observe2(this) { (action, storageId, specId) ->
            val args = ItemContentsFragmentArgs(storageId = storageId, specId = specId)
            findNavController().navigate(R.id.action_storageItemActionDialog_to_itemContentsFragment, args.toBundle())
        }

        super.onViewCreated(view, savedInstanceState)
    }
}