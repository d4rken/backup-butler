package eu.darken.bb.task.ui.tasklist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.view.clicks
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcs
import eu.darken.bb.processor.ui.ProcessorActivity
import eu.darken.bb.task.ui.tasklist.actions.TaskActionDialog
import javax.inject.Inject

class TaskListFragment : SmartFragment(), AutoInject, HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: TaskListFragmentVDC by vdcs { vdcSource }

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
