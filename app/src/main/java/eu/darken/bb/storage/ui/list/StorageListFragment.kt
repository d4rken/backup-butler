package eu.darken.bb.storage.ui.list

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.setupDefaults
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdcs
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

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.storage_list_fragment, container, false)
        addUnbinder(ButterKnife.bind(this, layout))
        return layout
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.editStorage(adapter.data[i]) })

        vdc.viewState.observe(this, Observer {
            adapter.update(it.storages)
        })

        fab.clicksDebounced().subscribe { vdc.createStorage() }

        vdc.editTaskEvent.observe(this, Observer {
            val bs = StorageActionDialog.newInstance(it.storageId)
            bs.show(childFragmentManager, it.storageId.toString())
        })

        super.onViewCreated(view, savedInstanceState)
    }
}
