package eu.darken.bb.backup.ui.generator.list

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding3.view.clicks
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.backup.ui.generator.list.actions.GeneratorsActionDialog
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.setupDefaults
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdcs
import javax.inject.Inject

class GeneratorsFragment : SmartFragment(), AutoInject, HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var vdcSource: VDCSource.Factory

    private val vdc: GeneratorsFragmentVDC by vdcs { vdcSource }
    @Inject lateinit var adapter: GeneratorsAdapter

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView
    @BindView(R.id.fab) lateinit var fab: FloatingActionButton

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector

    init {
        layoutRes = R.layout.generator_list_fragment
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        recyclerView.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.editGenerator(adapter.data[i]) })

        vdc.viewState.observe(this, Observer {
            adapter.update(it.generators)
        })

        fab.clicks().subscribe { vdc.newGenerator() }

        vdc.editTaskEvent.observe(this, Observer {
            val bs = GeneratorsActionDialog.newInstance(it.generatorId)
            bs.show(childFragmentManager, it.generatorId.toString())
        })

        super.onViewCreated(view, savedInstanceState)
    }
}
