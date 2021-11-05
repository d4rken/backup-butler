package eu.darken.bb.storage.ui.list.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.differ.update
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.observe2
import eu.darken.bb.common.toastError
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.databinding.StorageListActionDialogBinding
import eu.darken.bb.task.core.TaskRepo
import javax.inject.Inject

@AndroidEntryPoint
class StorageActionDialog : BottomSheetDialogFragment() {

    @Inject lateinit var taskRepo: TaskRepo
    @Inject lateinit var actionsAdapter: ActionsAdapter
    private val vdc: StorageActionDialogVDC by viewModels()
    private lateinit var ui: StorageListActionDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ui = StorageListActionDialogBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.recyclerview.setupDefaults(actionsAdapter)

        ui.workingOverlay.setOnCancelListener {
            vdc.cancelCurrentOperation()
        }

        vdc.state.observe2(this) { state ->
            if (state.storageInfo != null) {
                ui.typeLabel.text = getString(state.storageInfo.storageType.labelRes)
                ui.storageLabel.text = state.storageInfo.config?.label ?: getString(R.string.general_unknown_label)
            }

            actionsAdapter.update(state.allowedActions)

            ui.loadingIndicator.setGone(!state.isLoadingData)

            ui.recyclerview.setGone(state.isWorking)
            ui.workingOverlay.setGone(!state.isWorking)
            ui.workingOverlay.isCancelable = state.isCancelable
        }

        vdc.navEvents.observe2(this) { doNavigate(it) }
        vdc.closeDialogEvent.observe2(this) { dismissAllowingStateLoss() }
        vdc.errorEvent.observe2(this) { toastError(it) }

        super.onViewCreated(view, savedInstanceState)
    }
}
