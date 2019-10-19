package eu.darken.bb.storage.ui.viewer.content.page

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.backup.core.getBackupId
import eu.darken.bb.backup.core.getBackupSpecId
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.LoadingOverlayView
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.storage.core.getStorageId
import java.text.DateFormat
import javax.inject.Inject


class ContentPageFragment : SmartFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory

    private val vdc: ContentPageFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as ContentPageFragmentVDC.Factory
        factory.create(handle, requireArguments().getStorageId()!!, requireArguments().getBackupSpecId()!!, requireArguments().getBackupId()!!)
    })

    @Inject lateinit var adapter: ContentEntryAdapter

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView
    @BindView(R.id.creation_date_value) lateinit var creationDateValue: TextView
    @BindView(R.id.loading_overlay_infos) lateinit var loadingOverlayInfos: LoadingOverlayView
    @BindView(R.id.loading_overlay_files) lateinit var loadingOverlayFiles: LoadingOverlayView
    private val dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

    private var showRestoreAction = false

    init {
        layoutRes = R.layout.storage_viewer_item_content_adapter_page
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        requireActivityActionBar().setDisplayHomeAsUpEnabled(true)

        recyclerView.setupDefaults(adapter)

        vdc.state.observe(this, Observer { state ->
            if (state.metaData != null) {
                creationDateValue.text = dateFormatter.format(state.metaData.createdAt)
            }
            loadingOverlayInfos.setInvisible(!state.isLoadingInfos)

            adapter.update(state.items)
            recyclerView.setInvisible(state.isLoadingItems)
            loadingOverlayFiles.setInvisible(!state.isLoadingItems)

            loadingOverlayFiles.setError(state.error)

            showRestoreAction = state.showRestoreAction
            invalidateOptionsMenu()
        })

        vdc.finishEvent.observe(this, Observer {
            requireFragmentManager().popBackStack()
        })

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_storage_viewer_content_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_restore).isVisible = showRestoreAction
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            requireActivity().finish()
            true
        }
        R.id.action_restore -> {
            vdc.restore()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}