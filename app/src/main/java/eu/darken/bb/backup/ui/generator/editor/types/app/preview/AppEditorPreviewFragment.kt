package eu.darken.bb.backup.ui.generator.editor.types.app.preview

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.lists.ClickModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import javax.inject.Inject


class AppEditorPreviewFragment : SmartFragment(), AutoInject {

    val args by navArgs<AppEditorPreviewFragmentArgs>()

    @Inject lateinit var vdcSource: VDCSource.Factory
    val vdc: AppEditorPreviewFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as AppEditorPreviewFragmentVDC.Factory
        factory.create(handle, args.generatorId, args.previewMode)
    })
    @Inject lateinit var adapter: PkgsPreviewAdapter

    @BindView(R.id.pkg_preview_list) lateinit var pkgPreviewList: RecyclerView

    init {
        layoutRes = R.layout.generator_editor_app_preview_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().subtitle = getString(R.string.backuptype_app_label)

        pkgPreviewList.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.onSelect(adapter.data[i]) })

        vdc.state.observe(this, Observer { state ->
            adapter.update(state.pkgs)
        })

        super.onViewCreated(view, savedInstanceState)
    }


}
