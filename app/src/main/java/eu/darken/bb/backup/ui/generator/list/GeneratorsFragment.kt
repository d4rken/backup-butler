package eu.darken.bb.backup.ui.generator.list

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding4.view.clicks
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.backup.ui.generator.list.actions.GeneratorsActionDialog
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcs
import javax.inject.Inject

@AndroidEntryPoint
class GeneratorsFragment : SmartFragment() {

//    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
//    override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector

    @Inject lateinit var vdcSource: VDCSource.Factory

    private val vdc: GeneratorsFragmentVDC by vdcs { vdcSource }
    @Inject lateinit var adapter: GeneratorAdapter

    @BindView(R.id.generator_list) lateinit var generatorList: RecyclerView
    @BindView(R.id.fab) lateinit var fab: FloatingActionButton


    init {
        layoutRes = R.layout.generator_list_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        generatorList.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.editGenerator(adapter.data[i]) })

        vdc.viewState.observe2(this) {
            adapter.update(it.generators)
        }

        fab.clicks().subscribe { vdc.newGenerator() }

        vdc.editTaskEvent.observe2(this) {
            val bs = GeneratorsActionDialog.newInstance(it.generatorId)
            bs.show(childFragmentManager, it.generatorId.toString())
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
