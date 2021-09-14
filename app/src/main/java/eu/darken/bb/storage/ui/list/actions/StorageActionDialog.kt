package eu.darken.bb.storage.ui.list.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.toastError
import eu.darken.bb.common.ui.LoadingOverlayView
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.putStorageId
import eu.darken.bb.task.core.TaskRepo
import javax.inject.Inject

@AndroidEntryPoint
class StorageActionDialog : BottomSheetDialogFragment() {
    private var unbinder: Unbinder? = null

    @Inject lateinit var taskRepo: TaskRepo
    @Inject lateinit var actionsAdapter: ActionsAdapter
    private val vdc: StorageActionDialogVDC by viewModels()

    @BindView(R.id.type_label) lateinit var typeLabel: TextView
    @BindView(R.id.storage_label) lateinit var storageLabel: TextView
    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView
    @BindView(R.id.loading_indicator) lateinit var loadingIndicator: View
    @BindView(R.id.working_overlay) lateinit var workingOverlay: LoadingOverlayView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.storage_list_action_dialog, container, false)
        unbinder = ButterKnife.bind(this, layout)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.setupDefaults(actionsAdapter)

        actionsAdapter.modules.add(ClickModule { _: ActionsAdapter.VH, pos: Int ->
            val confirmable = actionsAdapter.data[pos]
            confirmable.guardedAction { vdc.storageAction(it) }
            actionsAdapter.notifyItemChanged(pos)
        })

        workingOverlay.setOnCancelListener {
            vdc.cancelCurrentOperation()
        }

        vdc.state.observe2(this) { state ->
            if (state.storageInfo != null) {
                typeLabel.text = getString(state.storageInfo.storageType.labelRes)
                storageLabel.text = state.storageInfo.config?.label ?: getString(R.string.general_unknown_label)
            }

            actionsAdapter.update(state.allowedActions)

            loadingIndicator.setGone(!state.isLoadingData)

            recyclerView.setGone(state.isWorking)
            workingOverlay.setGone(!state.isWorking)
            workingOverlay.isCancelable = state.isCancelable
        }

        vdc.closeDialogEvent.observe2(this) { dismissAllowingStateLoss() }
        vdc.errorEvent.observe2(this) { toastError(it) }

        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        fun newInstance(storageId: Storage.Id): BottomSheetDialogFragment = StorageActionDialog().apply {
            arguments = Bundle().putStorageId(storageId)
        }
    }
}