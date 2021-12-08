package eu.darken.bb.storage.ui.viewer.content

import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.Smart2Fragment
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.StorageViewerContentFragmentBinding

@AndroidEntryPoint
class StorageContentFragment : Smart2Fragment(R.layout.storage_viewer_content_fragment) {

    val navArgs by navArgs<StorageContentFragmentArgs>()
    override val ui: StorageViewerContentFragmentBinding by viewBinding()
    override val vdc: StorageContentFragmentVDC by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val pagerAdapter = VersionPagerAdapter(this, navArgs.storageId, navArgs.specId)

        ui.apply {
            viewpager.adapter = pagerAdapter
            tablayout.tabMode = TabLayout.MODE_SCROLLABLE
            toolbar.setupWithNavController(findNavController())
        }

        TabLayoutMediator(ui.tablayout, ui.viewpager) { tab, position ->
            tab.text = DateUtils.getRelativeTimeSpanString(pagerAdapter.data[position].createdAt.time)
        }.attach()

        vdc.state.observe2(this, ui) { state ->
            toolbar.apply {
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