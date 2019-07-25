package eu.darken.bb.storage.ui.editor.types.local

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdcs
import eu.darken.bb.storage.ui.list.StorageAdapter
import javax.inject.Inject


class LocalEditorFragment : SmartFragment(), AutoInject {
    companion object {
        fun newInstance(): Fragment = LocalEditorFragment()
    }

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: LocalEditorFragmentVDC by vdcs { vdcSource }

    @Inject lateinit var adapter: StorageAdapter

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView

    init {
        layoutRes = R.layout.storageeditor_local_fragment
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
//        recyclerView.itemAnimator = DefaultItemAnimator()
//        recyclerView.addItemDecoration(DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL))
//
//        recyclerView.adapter = adapter
//        vdc.viewState.observe(this, Observer {
//            adapter.apply {
//                data.clear()
//                data.addAll(it.storages)
//                notifyDataSetChanged()
//            }
//        })

        super.onViewCreated(view, savedInstanceState)
    }
}
