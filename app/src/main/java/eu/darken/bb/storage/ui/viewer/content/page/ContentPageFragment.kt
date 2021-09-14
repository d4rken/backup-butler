package eu.darken.bb.storage.ui.viewer.content.page


import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.LoadingOverlayView
import eu.darken.bb.common.ui.setInvisible
import java.text.DateFormat
import javax.inject.Inject

@AndroidEntryPoint
class ContentPageFragment : SmartFragment(R.layout.storage_viewer_item_content_adapter_page) {

    private val vdc: ContentPageFragmentVDC by viewModels()
    @Inject lateinit var adapter: ContentEntryAdapter

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView
    @BindView(R.id.creation_date_value) lateinit var creationDateValue: TextView
    @BindView(R.id.loading_overlay_infos) lateinit var loadingOverlayInfos: LoadingOverlayView
    @BindView(R.id.loading_overlay_files) lateinit var loadingOverlayFiles: LoadingOverlayView
    private val dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

    private var showRestoreAction = false

    init {
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        requireActivityActionBar().setDisplayHomeAsUpEnabled(true)

        recyclerView.setupDefaults(adapter)

        vdc.state.observe2(this) { state ->
            if (state.metaData != null) {
                creationDateValue.text = dateFormatter.format(state.metaData.createdAt)
            }
            loadingOverlayInfos.setInvisible(!state.isLoadingInfos)

            adapter.update(state.items)
            recyclerView.setInvisible(state.isLoadingItems)
            loadingOverlayFiles.setInvisible(!state.isLoadingItems)

            loadingOverlayFiles.updateWith(state.error)

            showRestoreAction = state.showRestoreAction
            invalidateOptionsMenu()
        }

        vdc.finishEvent.observe2(this) {
            findNavController().popBackStack()
        }

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
        R.id.action_restore -> {
            vdc.restore()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}