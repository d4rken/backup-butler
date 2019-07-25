package eu.darken.bb.storage.ui.editor.types

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
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
import eu.darken.bb.common.vdcs
import javax.inject.Inject


class TypeSelectionFragment : SmartFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: TypeSelectionFragmentVDC by vdcs { vdcSource }

    @Inject lateinit var adapter: TypeSelectionAdapter

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView

    init {
        layoutRes = R.layout.storageeditor_typeselection_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL))
        recyclerView.adapter = adapter

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.createType(adapter.data[i]) })

        vdc.state.observe(this, Observer {
            adapter.apply {
                data.clear()
                data.addAll(it.supportedTypes)
                notifyDataSetChanged()
            }
        })

        super.onViewCreated(view, savedInstanceState)
    }
}
