package eu.darken.bb.task.ui.tasklist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
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
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.setupDefaults
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
    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView
    @BindView(R.id.fab) lateinit var fab: FloatingActionButton

    var snackbar: Snackbar? = null

    init {
        layoutRes = R.layout.task_list_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        recyclerView.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.editTask(adapter.data[i].task) })

        vdc.state.observe(this, Observer { state ->
            adapter.update(state.tasks)

            if (state.hasRunningTask && snackbar == null) {
                Snackbar.make(view, R.string.label_progress_processing_task, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.action_show) {
                            startActivity(Intent(requireContext(), ProcessorActivity::class.java))
                        }
                        .addCallback(object : Snackbar.Callback() {
                            override fun onShown(sb: Snackbar?) {
                                snackbar = sb
                            }

                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                snackbar = null
                            }
                        })
                        .show()
            } else {
                snackbar?.dismiss()
            }
        })

        fab.clicks().subscribe { vdc.newTask() }

        vdc.editTaskEvent.observe(this, Observer {
            val bs = TaskActionDialog.newInstance(it.taskId)
            bs.show(childFragmentManager, it.taskId.toString())
        })
        super.onViewCreated(view, savedInstanceState)
    }
}
