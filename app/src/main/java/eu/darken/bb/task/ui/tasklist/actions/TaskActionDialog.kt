package eu.darken.bb.task.ui.tasklist.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.differ.update
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.Smart2BottomSheetDialogFragment
import eu.darken.bb.databinding.TaskListActionDialogBinding
import eu.darken.bb.task.core.TaskRepo
import javax.inject.Inject

@AndroidEntryPoint
class TaskActionDialog : Smart2BottomSheetDialogFragment() {
    override val vdc: TaskActionDialogVDC by viewModels()
    override lateinit var ui: TaskListActionDialogBinding
    @Inject lateinit var taskRepo: TaskRepo
    @Inject lateinit var actionsAdapter: ActionsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ui = TaskListActionDialogBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.recyclerview.setupDefaults(actionsAdapter)

        vdc.state.observe2(this, ui) { state ->
            taskTypeLabel.setText(state.taskType?.labelRes ?: R.string.general_unknown_label)
            taskName.text = state.taskName

            actionsAdapter.update(state.allowedActions)

            ui.recyclerview.visibility = if (state.loading) View.INVISIBLE else View.VISIBLE
            ui.progressCircular.visibility = if (state.loading) View.VISIBLE else View.INVISIBLE
            if (state.finished) dismissAllowingStateLoss()
        }

        super.onViewCreated(view, savedInstanceState)
    }
}