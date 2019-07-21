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
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdcs
import javax.inject.Inject


class TaskListFragment : SmartFragment(), AutoInject {
    companion object {
        fun newInstance(): Fragment = TaskListFragment()
    }

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: TaskListFragmentVDC by vdcs { vdcSource }

    @Inject lateinit var adapter: TaskListAdapter

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView


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

        super.onViewCreated(view, savedInstanceState)
    }
}
