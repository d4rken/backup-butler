package eu.darken.bb.backup.ui.generator.editor.types.app.preview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.getCountString
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.ClickMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.observe2
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.GeneratorEditorAppPreviewFragmentBinding
import javax.inject.Inject

@AndroidEntryPoint
class AppEditorPreviewFragment : SmartFragment(R.layout.generator_editor_app_preview_fragment) {

    val args by navArgs<AppEditorPreviewFragmentArgs>()

    private val vdc: AppEditorPreviewFragmentVDC by viewModels()
    private val ui: GeneratorEditorAppPreviewFragmentBinding by viewBinding()
    @Inject lateinit var adapter: PkgsPreviewAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.pkgPreviewList.setupDefaults(adapter)

        adapter.modules.add(ClickMod { _: ModularAdapter.VH, i: Int -> vdc.onSelect(adapter.data[i]) })

        vdc.state.observe2(this) { state ->
            adapter.update(state.pkgs)
            ui.pkgPreviewListWrapper.updateLoadingState(state.isLoading)

            when (state.previewMode) {
                PreviewMode.PREVIEW -> {
                    requireActivityActionBar().subtitle = getString(R.string.general_preview_label)
                    ui.infoMode.setText(R.string.backup_generator_app_itempreview_desc)
                    ui.infoItems.text = resources.getCountString(R.plurals.x_items, state.pkgs.size)
                }
                PreviewMode.INCLUDE -> {
                    requireActivityActionBar().subtitle = getString(R.string.general_included_label)
                    ui.infoMode.setText(R.string.backup_generator_app_includeditems_desc)
                    ui.infoItems.text = "${
                        resources.getCountString(
                            R.plurals.x_items,
                            state.pkgs.size
                        )
                    } (${resources.getCountString(R.plurals.x_selected, state.selected.size)})"
                }
                PreviewMode.EXCLUDE -> {
                    requireActivityActionBar().subtitle = getString(R.string.general_excluded_label)
                    ui.infoMode.setText(R.string.backup_generator_app_excludeditems_desc)
                    ui.infoItems.text = "${
                        resources.getCountString(
                            R.plurals.x_items,
                            state.pkgs.size
                        )
                    } (${resources.getCountString(R.plurals.x_selected, state.selected.size)})"
                }
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

}
