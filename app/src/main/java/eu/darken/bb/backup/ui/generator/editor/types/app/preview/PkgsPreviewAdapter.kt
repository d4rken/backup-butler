package eu.darken.bb.backup.ui.generator.editor.types.app.preview

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.DataAdapter
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.SimpleVHCreatorMod
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import eu.darken.bb.common.previews.AppPreviewRequest
import eu.darken.bb.common.previews.GlideApp
import eu.darken.bb.common.previews.into
import eu.darken.bb.databinding.GeneratorEditorAppPreviewAdapterLineBinding
import javax.inject.Inject


class PkgsPreviewAdapter @Inject constructor(
    pkgOps: PkgOps
) : ModularAdapter<PkgsPreviewAdapter.VH>(), DataAdapter<AppEditorPreviewFragmentVDC.PkgWrap> {

    override val data = mutableListOf<AppEditorPreviewFragmentVDC.PkgWrap>()

    init {
        modules.add(DataBinderMod(data))
        modules.add(SimpleVHCreatorMod { VH(it, pkgOps) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup, private val pkgOps: PkgOps) :
        ModularAdapter.VH(R.layout.generator_editor_app_preview_adapter_line, parent),
        BindableVH<AppEditorPreviewFragmentVDC.PkgWrap, GeneratorEditorAppPreviewAdapterLineBinding> {

        override val viewBinding = lazy { GeneratorEditorAppPreviewAdapterLineBinding.bind(itemView) }

        override val onBindData: GeneratorEditorAppPreviewAdapterLineBinding.(
            item: AppEditorPreviewFragmentVDC.PkgWrap,
            payloads: List<Any>
        ) -> Unit = { item, _ ->
            val pkg = item.pkg
            val isSelected = item.isSelected
            val mode = item.mode

            name.text = pkg.getLabel(pkgOps)
            when (mode) {
                PreviewMode.PREVIEW -> {
                    description.setTextColor(getColorForAttr(R.attr.colorOnBackground))
                    description.text = pkg.packageName
                }
                PreviewMode.INCLUDE -> {
                    description.setTextColor(getColorForAttr(R.attr.colorAccent))
                    description.text = if (isSelected) getString(R.string.general_included_label) else ""
                }
                PreviewMode.EXCLUDE -> {
                    description.setTextColor(getColorForAttr(R.attr.colorError))
                    description.text = if (isSelected) getString(R.string.general_excluded_label) else ""
                }
            }

            GlideApp.with(context)
                .load(AppPreviewRequest(item.pkg, context))
                .into(previewContainer)
        }
    }
}