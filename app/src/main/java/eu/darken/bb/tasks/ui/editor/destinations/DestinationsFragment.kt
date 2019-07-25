package eu.darken.bb.tasks.ui.editor.destinations

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdcsAssisted
import eu.darken.bb.tasks.core.getTaskId
import javax.inject.Inject


class DestinationsFragment : SmartFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: DestinationsFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as DestinationsFragmentVDC.Factory
        factory.create(handle, arguments!!.getTaskId()!!)
    })

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView

    @Inject lateinit var adapter: DestinationsAdapter

    init {
        layoutRes = R.layout.newtask_destinations_fragment
    }


    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL))
        recyclerView.adapter = adapter

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> })


        super.onViewCreated(view, savedInstanceState)
    }
}
