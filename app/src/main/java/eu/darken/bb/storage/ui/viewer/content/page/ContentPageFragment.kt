package eu.darken.bb.storage.ui.viewer.content.page


import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.StorageViewerItemContentAdapterPageBinding
import java.text.DateFormat
import javax.inject.Inject

@AndroidEntryPoint
class ContentPageFragment : SmartFragment(R.layout.storage_viewer_item_content_adapter_page) {

    private val vdc: ContentPageFragmentVDC by viewModels()
    private val ui: StorageViewerItemContentAdapterPageBinding by viewBinding()
    @Inject lateinit var adapter: ContentEntryAdapter

    private val dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

    private var showRestoreAction = false

    init {
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        requireActivityActionBar().setDisplayHomeAsUpEnabled(true)

        ui.recyclerview.setupDefaults(adapter)

        vdc.state.observe2(this, ui) { state ->
            if (state.metaData != null) {
                creationDateValue.text = dateFormatter.format(state.metaData.createdAt)
            }
            loadingOverlayInfos.setInvisible(!state.isLoadingInfos)

            adapter.update(state.items)
            recyclerview.setInvisible(state.isLoadingItems)
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