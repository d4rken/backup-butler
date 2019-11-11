package eu.darken.bb.common.file.picker.local

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.file.APath
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.BreadCrumbBar
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import javax.inject.Inject


class LocalPickerFragment : SmartFragment(), AutoInject {

    val navArgs by navArgs<LocalPickerFragmentArgs>()

    @Inject lateinit var vdcSource: VDCSource.Factory
    val vdc: LocalPickerFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as LocalPickerFragmentVDC.Factory
        factory.create(handle, navArgs.options)
    })

    @BindView(R.id.breadcrumb_bar) lateinit var breadCrumbBar: BreadCrumbBar<APath>
    @BindView(R.id.files_list) lateinit var filesList: RecyclerView

    @Inject lateinit var adapter: LocalPathAdapter

    init {
        layoutRes = R.layout.pathpicker_local_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        breadCrumbBar.crumbNamer = {
            it.name
        }

        filesList.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.selectItem(adapter.data[i]) })

        vdc.state.observe2(this) { state ->
            breadCrumbBar.setCrumbs(state.currentCrumbs)
            adapter.update(state.currentListing)
        }

        super.onViewCreated(view, savedInstanceState)
    }


}
