package eu.darken.bb.tasks.ui.editor.destinations

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
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
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.setupDefaults
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdcsAssisted
import eu.darken.bb.storage.ui.list.StorageAdapter
import eu.darken.bb.tasks.core.getTaskId
import javax.inject.Inject
import javax.inject.Provider


class DestinationsFragment : SmartFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: DestinationsFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as DestinationsFragmentVDC.Factory
        factory.create(handle, arguments!!.getTaskId()!!)
    })

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView
    @BindView(R.id.add_destination) lateinit var addDestination: Button

    @Inject lateinit var adapter: StorageAdapter
    @Inject lateinit var pickerAdapterProvider: Provider<StorageAdapter>

    init {
        layoutRes = R.layout.newtask_destinations_fragment
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL))
        recyclerView.adapter = adapter

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.removeDestination(adapter.data[i]) })

        addDestination.clicksDebounced().subscribe { vdc.showDestinationPicker() }

        vdc.state.observe(this, Observer { state ->
            adapter.apply {
                data.clear()
                data.addAll(state.destinations)
                notifyDataSetChanged()
            }
        })

        vdc.storagePickerEvent.observe(this, Observer { availableStorages ->
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(R.layout.generic_recyclerview_dialog)
            val pickerDialog = builder.create()
            pickerDialog.show()

            val pickerRecycler = pickerDialog.findViewById<RecyclerView>(R.id.recyclerview)!!
            pickerRecycler.setupDefaults()

            val pickerAdapter = pickerAdapterProvider.get()
            pickerAdapter.apply {
                data.addAll(availableStorages)
            }
            pickerRecycler.adapter = pickerAdapter
            pickerAdapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int ->
                vdc.addDestination(pickerAdapter.data[i])
                pickerDialog.dismiss()
            })
        })
        super.onViewCreated(view, savedInstanceState)
    }
}
