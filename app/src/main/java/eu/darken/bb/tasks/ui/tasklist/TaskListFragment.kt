package eu.darken.bb.tasks.ui.tasklist

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding3.view.clicks
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdcs
import eu.darken.bb.tasks.ui.tasklist.actions.TaskActionDialog
import javax.inject.Inject

class TaskListFragment : SmartFragment(), AutoInject, HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var vdcSource: VDCSource.Factory

    private val vdc: TaskListFragmentVDC by vdcs { vdcSource }
    @Inject lateinit var adapter: TaskListAdapter

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView
    @BindView(R.id.fab) lateinit var fab: FloatingActionButton

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.task_list_fragment, container, false)
        addUnbinder(ButterKnife.bind(this, layout))
        return layout
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL))
        recyclerView.adapter = adapter

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.editTask(adapter.data[i]) })

        vdc.viewState.observe(this, Observer {
            adapter.data.clear()
            adapter.data.addAll(it.repos)
            adapter.notifyDataSetChanged()
        })

        fab.clicks().subscribe { vdc.newTask() }

        vdc.editTaskEvent.observe(this, Observer {
            val bs = TaskActionDialog.newInstance(it.taskId)
            bs.show(childFragmentManager, it.taskId.toString())
        })

        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        fun newInstance(): Fragment = TaskListFragment()
    }
}
