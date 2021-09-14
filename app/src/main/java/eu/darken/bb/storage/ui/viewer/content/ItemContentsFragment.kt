package eu.darken.bb.storage.ui.viewer.content

import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import butterknife.BindView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.toastError
import eu.darken.bb.common.ui.LoadingOverlayView
import eu.darken.bb.common.ui.setInvisible

@AndroidEntryPoint
class ItemContentsFragment : SmartFragment(R.layout.storage_viewer_itemcontent_fragment) {

    val navArgs by navArgs<ItemContentsFragmentArgs>()
    private val vdc: ItemContentsFragmentVDC by viewModels()

    private val pagerAdapter by lazy { VersionPagerAdapter(this, navArgs.storageId, navArgs.specId) }

    @BindView(R.id.loading_overlay) lateinit var loadingOverlayView: LoadingOverlayView
    @BindView(R.id.viewpager_container) lateinit var viewPagerContainer: ViewGroup
    @BindView(R.id.viewpager) lateinit var viewPager: ViewPager2
    @BindView(R.id.tablayout) lateinit var tabLayout: TabLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewPager.adapter = pagerAdapter
        tabLayout.tabMode = TabLayout.MODE_SCROLLABLE

        TabLayoutMediator(tabLayout, viewPager, TabLayoutMediator.TabConfigurationStrategy { tab, position ->
            tab.text = DateUtils.getRelativeTimeSpanString(pagerAdapter.data[position].createdAt.time)
        }).attach()

        vdc.state.observe2(this) { state ->
            requireActivityActionBar().apply {
                title = state.backupSpec?.getLabel(requireContext()) ?: getString(R.string.progress_loading_label)
                if (state.backupSpec != null) setSubtitle(state.backupSpec.backupType.labelRes)
            }

            pagerAdapter.update(state.versions)

            loadingOverlayView.setInvisible(state.versions != null)
            viewPagerContainer.setInvisible(state.versions == null)
        }

        vdc.errorEvent.observe2(this) {
            toastError(it)
        }
        vdc.finishEvent.observe2(this) {
            findNavController().popBackStack()
        }

        super.onViewCreated(view, savedInstanceState)
    }
}