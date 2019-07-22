package eu.darken.bb.tasks.ui.tasklist

import android.annotation.SuppressLint
import android.content.Intent
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
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdcs
import eu.darken.bb.tasks.ui.newtask.NewTaskActivity
import javax.inject.Inject


class TaskListFragment : SmartFragment(), AutoInject {
    @Inject lateinit var vdcSource: VDCSource.Factory

    private val vdc: TaskListFragmentVDC by vdcs { vdcSource }
    @Inject lateinit var adapter: TaskListAdapter

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView
    @BindView(R.id.fab) lateinit var fab: FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.tasklist_fragment, container, false)
        addUnbinder(ButterKnife.bind(this, layout))
        return layout
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL))

        recyclerView.adapter = adapter
        vdc.viewState.observe(this, Observer {
            adapter.data = it.repos
            adapter.notifyDataSetChanged()
        })

        fab.clicks().subscribe { vdc.newTask() }
        vdc.newTaskEvent.observe(this, Observer {
            startActivity(Intent(requireContext(), NewTaskActivity::class.java))
        })

        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        fun newInstance(): Fragment = TaskListFragment()
    }
}
