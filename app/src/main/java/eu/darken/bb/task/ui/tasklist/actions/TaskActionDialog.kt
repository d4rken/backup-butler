package eu.darken.bb.task.ui.tasklist.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
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
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskRepo
import eu.darken.bb.task.core.putTaskId
import javax.inject.Inject

@AndroidEntryPoint
class TaskActionDialog : BottomSheetDialogFragment() {
    private var unbinder: Unbinder? = null

    @Inject lateinit var taskRepo: TaskRepo
    @Inject lateinit var actionsAdapter: ActionsAdapter

    private val vdc: TaskActionDialogVDC by viewModels()

    @BindView(R.id.task_type_label) lateinit var taskTypeLabel: TextView
    @BindView(R.id.task_name) lateinit var taskName: TextView
    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView
    @BindView(R.id.progress_circular) lateinit var progressBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.task_list_action_dialog, container, false)
        unbinder = ButterKnife.bind(this, layout)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.setupDefaults(actionsAdapter)

        actionsAdapter.modules.add(ClickModule { _: ModularAdapter.VH, pos: Int ->
            actionsAdapter.data[pos].guardedAction { vdc.taskAction(it) }
            actionsAdapter.notifyItemChanged(pos)
        })

        vdc.state.observe2(this) { state ->
            taskTypeLabel.setText(state.taskType?.labelRes ?: R.string.general_unknown_label)
            taskName.text = state.taskName

            actionsAdapter.update(state.allowedActions)

            recyclerView.visibility = if (state.loading) View.INVISIBLE else View.VISIBLE
            progressBar.visibility = if (state.loading) View.VISIBLE else View.INVISIBLE
            if (state.finished) dismissAllowingStateLoss()
        }

        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        fun newInstance(taskId: Task.Id): BottomSheetDialogFragment = TaskActionDialog().apply {
            arguments = Bundle().putTaskId(taskId)
        }
    }
}