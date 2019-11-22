package eu.darken.bb.storage.ui.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.RecyclerViewWrapperLayout
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcs
import eu.darken.bb.storage.ui.list.actions.StorageActionDialog
import javax.inject.Inject


class StorageListFragment : SmartFragment(), AutoInject, HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: StorageListFragmentVDC by vdcs { vdcSource }

    @Inject lateinit var adapter: StorageAdapter
    @BindView(R.id.storage_list) lateinit var storageList: RecyclerView
    @BindView(R.id.storage_list_wrapper) lateinit var storageListWrapper: RecyclerViewWrapperLayout
    @BindView(R.id.fab) lateinit var fab: FloatingActionButton

    init {
        layoutRes = R.layout.storage_list_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        storageList.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.editStorage(adapter.data[i]) })

        vdc.storageData.observe2(this) { state ->
            adapter.update(state.storages)

            storageListWrapper.updateLoadingState(state.isLoading)

            fab.setInvisible(state.isLoading)

            requireActivity().invalidateOptionsMenu()
        }

        fab.clicksDebounced().subscribe { vdc.createStorage() }

        vdc.editTaskEvent.observe2(this) {
            val bs = StorageActionDialog.newInstance(it)
            bs.show(childFragmentManager, it.toString())
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
