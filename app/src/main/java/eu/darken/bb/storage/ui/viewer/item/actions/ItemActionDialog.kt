package eu.darken.bb.storage.ui.viewer.item.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import butterknife.ButterKnife
import butterknife.Unbinder
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.toastError
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.StorageViewerContentlistActionDialogBinding
import eu.darken.bb.storage.ui.viewer.StorageViewerActivity
import eu.darken.bb.storage.ui.viewer.content.ItemContentsFragmentArgs
import eu.darken.bb.task.core.TaskRepo
import javax.inject.Inject

@AndroidEntryPoint
class ItemActionDialog : BottomSheetDialogFragment() {

    private val vdc: ItemActionDialogVDC by viewModels()
    private val activityVdc by lazy { (requireActivity() as StorageViewerActivity).vdc }

    private var unbinder: Unbinder? = null

    @Inject lateinit var taskRepo: TaskRepo
    @Inject lateinit var actionsAdapter: ActionsAdapter

    private val ui: StorageViewerContentlistActionDialogBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.storage_viewer_contentlist_action_dialog, container, false)
        unbinder = ButterKnife.bind(this, layout)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.actionlist.setupDefaults(actionsAdapter)

        actionsAdapter.modules.add(ClickMod { _: ModularAdapter.VH, pos: Int ->
            actionsAdapter.data[pos].guardedAction { vdc.storageAction(it) }
            actionsAdapter.notifyItemChanged(pos)
        })

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

        vdc.finishedEvent.observe2(this) { dismissAllowingStateLoss() }

        vdc.errorEvents.observe2(this) { toastError(it) }

        super.onViewCreated(view, savedInstanceState)
    }
}