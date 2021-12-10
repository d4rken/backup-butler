package eu.darken.bb.storage.ui.viewer.content.page


import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.smart.Smart2Fragment
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.StorageViewerContentPageFragmentBinding
import eu.darken.bb.storage.ui.viewer.content.StorageContentFragment
import java.text.DateFormat
import javax.inject.Inject

@AndroidEntryPoint
class ContentPageFragment : Smart2Fragment(R.layout.storage_viewer_content_page_fragment) {

    override val vdc: ContentPageFragmentVDC by viewModels()
    override val ui: StorageViewerContentPageFragmentBinding by viewBinding()
    @Inject lateinit var adapter: ContentItemAdapter

    private val dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

    private var showRestoreAction = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.recyclerview.setupDefaults(adapter)

        vdc.state.observe2(ui) { state ->
            if (state.metaData != null) {
                creationDateValue.text = dateFormatter.format(state.metaData.createdAt)
            }
            loadingOverlayInfos.setInvisible(!state.isLoadingInfos)

            adapter.update(state.items)
            recyclerview.setInvisible(state.isLoadingItems)
            loadingOverlayFiles.setInvisible(!state.isLoadingItems)

            loadingOverlayFiles.updateWith(state.error)

            showRestoreAction = state.showRestoreAction
            if (isResumed) setupToolbar(getParentToolbar())
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        setupToolbar(getParentToolbar())
        super.onResume()
    }

    private fun getParentToolbar(): Toolbar {
        return (requireParentFragment() as StorageContentFragment).ui.toolbar
    }

    private fun setupToolbar(toolbar: Toolbar) {
        toolbar.apply {
            menu.clear()
            inflateMenu(R.menu.menu_storage_viewer_content_fragment)
            setOnCreateContextMenuListener { menu, _, _ ->
                menu.findItem(R.id.action_restore)?.isVisible = showRestoreAction
            }
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_restore -> {
                        vdc.restore()
                        true
                    }
                    else -> super.onOptionsItemSelected(item)
                }
            }
        }
    }
}