package eu.darken.bb.storage.ui.list

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
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdcs
import javax.inject.Inject


class StorageListFragment : SmartFragment(), AutoInject {
    companion object {
        fun newInstance(): Fragment = StorageListFragment()
    }

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: StorageListFragmentVDC by vdcs { vdcSource }

    @Inject lateinit var adapter: StorageAdapter

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView
    @BindView(R.id.fab) lateinit var fab: FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.storagelist_fragment, container, false)
        addUnbinder(ButterKnife.bind(this, layout))
        return layout
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL))

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.editStorage(adapter.data[i]) })

        recyclerView.adapter = adapter
        vdc.viewState.observe(this, Observer {
            adapter.apply {
                data.clear()
                data.addAll(it.storages)
                notifyDataSetChanged()
            }
        })

        fab.clicksDebounced().subscribe { vdc.createStorage() }

        super.onViewCreated(view, savedInstanceState)
    }
}
