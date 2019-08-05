package eu.darken.bb.tasks.ui.editor.sources

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.backups.ui.generator.list.GeneratorsAdapter
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
import eu.darken.bb.tasks.core.getTaskId
import javax.inject.Inject
import javax.inject.Provider


class SourcesFragment : SmartFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: SourcesFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as SourcesFragmentVDC.Factory
        factory.create(handle, arguments!!.getTaskId()!!)
    })

    @BindView(R.id.recyclerview_selected) lateinit var selectedList: RecyclerView
    @BindView(R.id.add_source) lateinit var addSource: Button

    @Inject lateinit var adapter: GeneratorsAdapter
    @Inject lateinit var pickerAdapterProvider: Provider<GeneratorsAdapter>

    init {
        layoutRes = R.layout.task_editor_sources_fragment
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().setSubtitle(R.string.label_sources)

        selectedList.setupDefaults(adapter)
        addSource.clicksDebounced().subscribe { vdc.showSourcePicker() }

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.removeSource(adapter.data[i]) })

        vdc.state.observe(this, Observer { state ->
            adapter.update(state.sources)
        })

        vdc.sourcePickerEvent.observe(this, Observer { availableSources ->
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(R.layout.generic_recyclerview_dialog)
            val pickerDialog = builder.create()
            pickerDialog.show()

            val pickerRecycler = pickerDialog.findViewById<RecyclerView>(R.id.recyclerview)!!
            val pickerAdapter = pickerAdapterProvider.get()
            pickerRecycler.setupDefaults(pickerAdapter)

            pickerAdapter.update(availableSources)

            pickerAdapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int ->
                vdc.addSource(pickerAdapter.data[i])
                pickerDialog.dismiss()
            })
        })
        super.onViewCreated(view, savedInstanceState)
    }
}
