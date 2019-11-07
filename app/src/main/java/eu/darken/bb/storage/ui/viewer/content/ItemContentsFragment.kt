package eu.darken.bb.storage.ui.viewer.content

import android.os.Bundle
import android.text.format.DateUtils
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import butterknife.BindView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.backup.core.getBackupSpecId
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.toastError
import eu.darken.bb.common.ui.LoadingOverlayView
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.storage.core.getStorageId
import javax.inject.Inject


class ItemContentsFragment : SmartFragment(), AutoInject, HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector

    @Inject lateinit var vdcSource: VDCSource.Factory

    private val vdc: ItemContentsFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as ItemContentsFragmentVDC.Factory
        factory.create(handle, arguments!!.getStorageId()!!, arguments!!.getBackupSpecId()!!)
    })

    private val pagerAdapter by lazy { VersionPagerAdapter(this, arguments!!.getStorageId()!!, arguments!!.getBackupSpecId()!!) }


    @BindView(R.id.loading_overlay) lateinit var loadingOverlayView: LoadingOverlayView
    @BindView(R.id.viewpager_container) lateinit var viewPagerContainer: ViewGroup
    @BindView(R.id.viewpager) lateinit var viewPager: ViewPager2
    @BindView(R.id.tablayout) lateinit var tabLayout: TabLayout

    init {
        layoutRes = R.layout.storage_viewer_itemcontent_fragment
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        requireActivityActionBar().setDisplayHomeAsUpEnabled(true)

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
            requireFragmentManager().popBackStackImmediate()
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            requireFragmentManager().popBackStackImmediate()
            true
        }

        else -> super.onOptionsItemSelected(item)
    }
}