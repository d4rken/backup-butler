package eu.darken.bb.task.ui.tasklist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding4.view.clicks
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.processor.ui.ProcessorActivity
import eu.darken.bb.task.ui.tasklist.actions.TaskActionDialog
import javax.inject.Inject

@AndroidEntryPoint
class TaskListFragment : SmartFragment() {

    private val vdc: TaskListFragmentVDC by viewModels()

    @Inject lateinit var adapter: TaskListAdapter
    @BindView(R.id.tasks_list) lateinit var tasksList: RecyclerView
    @BindView(R.id.fab) lateinit var fab: FloatingActionButton


    init {
        layoutRes = R.layout.task_list_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tasksList.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.editTask(adapter.data[i].task) })


        vdc.state.observe2(this) { state ->
            adapter.update(state.tasks)
        }

        fab.clicks().subscribe { vdc.newTask() }

        vdc.editTaskEvent.observe2(this) {
            val bs = TaskActionDialog.newInstance(it.taskId)
            bs.show(childFragmentManager, it.taskId.toString())
        }

        var snackbar: Snackbar? = null
        vdc.processorEvent.observe2(this) { isActive ->
            if (isVisible && isActive && snackbar == null) {
                snackbar = Snackbar.make(view, R.string.progress_processing_task_label, Snackbar.LENGTH_INDEFINITE)
                    .setAnchorView(fab)
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
