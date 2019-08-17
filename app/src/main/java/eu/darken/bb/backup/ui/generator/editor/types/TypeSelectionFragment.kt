package eu.darken.bb.backup.ui.generator.editor.types

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.backup.core.getGeneratorId
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.setupDefaults
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import javax.inject.Inject


class TypeSelectionFragment : SmartFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: TypeSelectionFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as TypeSelectionFragmentVDC.Factory
        factory.create(handle, arguments!!.getGeneratorId()!!)
    })

    @Inject lateinit var adapter: TypeSelectionAdapter

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView

    init {
        layoutRes = R.layout.generator_editor_typeselection_fragment
    }

    override fun onAttach(context: Context) {
        setHasOptionsMenu(true)
        requireActivityActionBar().setDisplayHomeAsUpEnabled(true)
        requireActivityActionBar().setHomeAsUpIndicator(R.drawable.ic_cancel)
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                vdc.dismiss()
            }
        })
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.createType(adapter.data[i]) })

        vdc.state.observe(this, Observer {
            adapter.update(it.supportedTypes)
        })

        vdc.finishActivity.observe(this, Observer { requireActivity().finish() })

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> vdc.dismiss()
        else -> super.onOptionsItemSelected(item)
    }
}
