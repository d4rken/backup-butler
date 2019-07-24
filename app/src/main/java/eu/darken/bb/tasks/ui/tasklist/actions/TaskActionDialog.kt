package eu.darken.bb.tasks.ui.tasklist.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.vdcsAssisted
import eu.darken.bb.tasks.core.BackupTaskRepo
import eu.darken.bb.tasks.core.getTaskId
import eu.darken.bb.tasks.core.putTaskId
import java.util.*
import javax.inject.Inject

class TaskActionDialog : BottomSheetDialogFragment(), AutoInject {
    private var unbinder: Unbinder? = null

    @Inject lateinit var taskRepo: BackupTaskRepo
    @Inject lateinit var dialogHelper: DialogHelper
    @Inject lateinit var actionsAdapter: ActionsAdapter

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: TaskActionDialogVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as TaskActionDialogVDC.Factory
        factory.create(handle, arguments!!.getTaskId()!!)
    })

    @BindView(R.id.task_name) lateinit var taskName: TextView
    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView
    @BindView(R.id.progress_circular) lateinit var progressBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.tasklist_action_dialog, container, false)
        unbinder = ButterKnife.bind(this, layout)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL))
        recyclerView.adapter = actionsAdapter

        actionsAdapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int ->
            vdc.taskAction(actionsAdapter.data[i])
        })

        vdc.state.observe(this, Observer { state ->
            taskName.text = state.taskName

            actionsAdapter.apply {
                data.clear()
                data.addAll(state.allowedActions)
                notifyDataSetChanged()
            }
            recyclerView.visibility = if (state.loading) View.INVISIBLE else View.VISIBLE
            progressBar.visibility = if (state.loading) View.VISIBLE else View.INVISIBLE
            if (state.finished) dismissAllowingStateLoss()
        })
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        fun newInstance(taskId: UUID): BottomSheetDialogFragment = TaskActionDialog().apply {
            arguments = Bundle().putTaskId(taskId)
        }
    }
}