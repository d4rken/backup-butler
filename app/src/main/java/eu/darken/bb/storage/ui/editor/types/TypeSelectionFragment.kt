package eu.darken.bb.storage.ui.editor.types

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.ui.BaseEditorFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.storage.core.getStorageId
import javax.inject.Inject


class TypeSelectionFragment : BaseEditorFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    override val vdc: TypeSelectionFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as TypeSelectionFragmentVDC.Factory
        factory.create(handle, arguments!!.getStorageId()!!)
    })


    @Inject lateinit var adapter: TypeSelectionAdapter

    @BindView(R.id.recyclerview) lateinit var recyclerView: RecyclerView

    init {
        layoutRes = R.layout.storage_editor_typeselection_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().setDisplayHomeAsUpEnabled(true)
        requireActivityActionBar().subtitle = getString(R.string.storage_selection_label)
        requireActivityActionBar().setHomeAsUpIndicator(R.drawable.ic_cancel)

        recyclerView.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.createType(adapter.data[i]) })

        vdc.state.observe2(this) {
            adapter.update(it.supportedTypes)
        }

        super.onViewCreated(view, savedInstanceState)
    }

}
