package eu.darken.bb.backup.ui.generator.editor.types.app.preview

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.getCountString
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
    @BindView(R.id.info_mode) lateinit var infoMode: TextView
    @BindView(R.id.info_items) lateinit var infoItems: TextView

    init {
        layoutRes = R.layout.generator_editor_app_preview_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().subtitle = getString(R.string.backuptype_app_label)

        pkgPreviewList.setupDefaults(adapter)

        adapter.modules.add(ClickModule { _: ModularAdapter.VH, i: Int -> vdc.onSelect(adapter.data[i]) })

        vdc.state.observe(this, Observer { state ->
            adapter.update(state.pkgs)

            when (state.previewMode) {
                PreviewMode.PREVIEW -> {
                    requireActivityActionBar().title = getString(R.string.label_preview)
                    infoMode.setText(R.string.generator_itempreview_desc)
                    infoItems.text = resources.getCountString(R.plurals.x_items, state.pkgs.size)
                }
                PreviewMode.INCLUDE -> {
                    requireActivityActionBar().title = getString(R.string.label_included)
                    infoMode.setText(R.string.generator_includeditems_desc)
                    infoItems.text = "${resources.getCountString(R.plurals.x_items, state.pkgs.size)} (${resources.getCountString(R.plurals.x_selected, state.selected.size)})"
                }
                PreviewMode.EXCLUDE -> {
                    requireActivityActionBar().title = getString(R.string.label_excluded)
                    infoMode.setText(R.string.generator_excludeditems_desc)
                    infoItems.text = "${resources.getCountString(R.plurals.x_items, state.pkgs.size)} (${resources.getCountString(R.plurals.x_selected, state.selected.size)})"
                }
            }
        })

        super.onViewCreated(view, savedInstanceState)
    }


}
