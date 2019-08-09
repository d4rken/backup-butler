package eu.darken.bb.storage.ui.viewer.details.page

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.setupDefaults
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdcsAssisted
import eu.darken.bb.storage.core.getBackupId
import eu.darken.bb.storage.core.getBackupSpecId
import eu.darken.bb.storage.core.getStorageId
import javax.inject.Inject


class DetailPageFragment : SmartFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory

    private val vdc: DetailPageFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as DetailPageFragmentVDC.Factory
        factory.create(handle, requireArguments().getStorageId()!!, requireArguments().getBackupSpecId()!!, requireArguments().getBackupId()!!)
    })

    @Inject lateinit var adapter: BackupItemAdapter

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView

    init {
        layoutRes = R.layout.storage_viewer_contentdetails_adapter_page
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        requireActivityActionBar().setDisplayHomeAsUpEnabled(true)

        recyclerView.setupDefaults(adapter)

        vdc.state.observe(this, Observer { state ->
            adapter.update(state.items)

        })

        vdc.finishEvent.observe(this, Observer {
            requireFragmentManager().popBackStack()
        })

        super.onViewCreated(view, savedInstanceState)
    }
}