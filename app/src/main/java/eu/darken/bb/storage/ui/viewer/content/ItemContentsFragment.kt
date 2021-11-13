package eu.darken.bb.storage.ui.viewer.content

import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.smart.Smart2Fragment
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.StorageViewerItemcontentFragmentBinding

@AndroidEntryPoint
class ItemContentsFragment : Smart2Fragment(R.layout.storage_viewer_itemcontent_fragment) {

    val navArgs by navArgs<ItemContentsFragmentArgs>()
    override val ui: StorageViewerItemcontentFragmentBinding by viewBinding()
    override val vdc: ItemContentsFragmentVDC by viewModels()

    private val pagerAdapter by lazy { VersionPagerAdapter(this, navArgs.storageId, navArgs.specId) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.viewpager.adapter = pagerAdapter
        ui.tablayout.tabMode = TabLayout.MODE_SCROLLABLE

        TabLayoutMediator(ui.tablayout, ui.viewpager) { tab, position ->
            tab.text = DateUtils.getRelativeTimeSpanString(pagerAdapter.data[position].createdAt.time)
        }.attach()

        vdc.state.observe2(this, ui) { state ->
            requireActivityActionBar().apply {
                if (state.backupSpec != null) title = getString(state.backupSpec.backupType.labelRes)
                subtitle = state.backupSpec?.getLabel(requireContext()) ?: getString(R.string.progress_loading_label)
            }

            pagerAdapter.update(state.versions)

            ui.loadingOverlay.setInvisible(state.versions != null)
            ui.viewpagerContainer.setInvisible(state.versions == null)
        }

        super.onViewCreated(view, savedInstanceState)
    }
}