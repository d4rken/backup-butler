package eu.darken.bb.storage.ui.viewer.details

import android.os.Bundle
import android.text.format.DateUtils
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import butterknife.BindView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdcsAssisted
import eu.darken.bb.storage.core.getBackupSpecId
import eu.darken.bb.storage.core.getStorageId
import javax.inject.Inject


class ContentDetailsFragment : SmartFragment(), AutoInject, HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector

    @Inject lateinit var vdcSource: VDCSource.Factory

    private val vdc: ContentDetailsFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as ContentDetailsFragmentVDC.Factory
        factory.create(handle, arguments!!.getStorageId()!!, arguments!!.getBackupSpecId()!!)
    })

    private val pagerAdapter by lazy { VersionPagerAdapter(this, arguments!!.getStorageId()!!, arguments!!.getBackupSpecId()!!) }


    @BindView(R.id.viewpager) lateinit var viewPager: ViewPager2
    @BindView(R.id.tablayout) lateinit var tabLayout: TabLayout

    init {
        layoutRes = R.layout.storage_viewer_contentdetails_fragment
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        requireActivityActionBar().setDisplayHomeAsUpEnabled(true)

        viewPager.adapter = pagerAdapter
        tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
        TabLayoutMediator(tabLayout, viewPager, TabLayoutMediator.OnConfigureTabCallback { tab, position ->
            tab.text = DateUtils.getRelativeTimeSpanString(pagerAdapter.data[position].createdAt.time)
        }).attach()


        vdc.state.observe(this, Observer { state ->
            if (state.backupSpec != null) {
                requireActivityActionBar().title = state.backupSpec.getLabel(requireContext())
            }
            pagerAdapter.update(state.versions)

        })

        vdc.finishEvent.observe(this, Observer {
            requireFragmentManager().popBackStack()
        })

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            requireFragmentManager().popBackStack()
            true
        }

        else -> super.onOptionsItemSelected(item)
    }
}