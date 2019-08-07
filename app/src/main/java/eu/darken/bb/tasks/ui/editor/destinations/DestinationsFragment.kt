package eu.darken.bb.tasks.ui.editor.destinations

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.requireActivityActionBar
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

    @BindView(R.id.recyclerview_selected) lateinit var selectedList: RecyclerView
    @BindView(R.id.add_destination) lateinit var addDestination: Button

    @Inject lateinit var adapter: StorageAdapter
    @Inject lateinit var pickerAdapterProvider: Provider<StorageAdapter>

    init {
        layoutRes = R.layout.task_editor_destinations_fragment
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().setSubtitle(R.string.label_destinations)

        selectedList.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.removeDestination(adapter.data[i]) })

        addDestination.clicksDebounced().subscribe { vdc.showDestinationPicker() }

        vdc.state.observe(this, Observer { state ->
            adapter.update(state.destinations)
        })

        vdc.storagePickerEvent.observe(this, Observer { availableStorages ->
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(R.layout.generic_recyclerview_dialog)
            val pickerDialog = builder.create()
            pickerDialog.show()

            val pickerRecycler = pickerDialog.findViewById<RecyclerView>(R.id.recyclerview)!!
            pickerRecycler.setupDefaults()

            val pickerAdapter = pickerAdapterProvider.get()
            pickerAdapter.update(availableStorages)

            pickerRecycler.adapter = pickerAdapter
            pickerAdapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int ->
                vdc.addDestination(pickerAdapter.data[i])
                pickerDialog.dismiss()
            })
        })
        super.onViewCreated(view, savedInstanceState)
    }
}
