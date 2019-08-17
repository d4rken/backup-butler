package eu.darken.bb.storage.ui.list

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
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
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.setupDefaults
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.LoadingOverlayView
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcs
import eu.darken.bb.storage.ui.list.actions.StorageActionDialog
import javax.inject.Inject


class StorageListFragment : SmartFragment(), AutoInject, HasSupportFragmentInjector {
    companion object {
        fun newInstance(): Fragment = StorageListFragment()
    }

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: StorageListFragmentVDC by vdcs { vdcSource }

    @Inject lateinit var adapter: StorageAdapter

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView
    @BindView(R.id.fab) lateinit var fab: FloatingActionButton
    @BindView(R.id.loading_overlay) lateinit var loadingOverlay: LoadingOverlayView

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector

    init {
        layoutRes = R.layout.storage_list_fragment
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.editStorage(adapter.data[i]) })

        vdc.state.observe(this, Observer { state ->
            adapter.update(state.storages)

            loadingOverlay.setInvisible(!state.isLoading)
            recyclerView.setInvisible(state.isLoading)
            fab.setInvisible(state.isLoading)
        })

        fab.clicksDebounced().subscribe { vdc.createStorage() }

        vdc.editTaskEvent.observe(this, Observer {
            val bs = StorageActionDialog.newInstance(it)
            bs.show(childFragmentManager, it.toString())
        })

        super.onViewCreated(view, savedInstanceState)
    }
}
