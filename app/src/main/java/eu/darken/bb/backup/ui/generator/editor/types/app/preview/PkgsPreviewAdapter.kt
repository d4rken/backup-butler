package eu.darken.bb.backup.ui.generator.editor.types.app.preview

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.DataAdapter
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.SimpleVHCreatorMod
import eu.darken.bb.common.previews.AppPreviewRequest
import eu.darken.bb.common.previews.GlideApp
import eu.darken.bb.common.previews.into
import eu.darken.bb.databinding.GeneratorEditorAppPreviewAdapterLineBinding
import javax.inject.Inject


class PkgsPreviewAdapter @Inject constructor() : ModularAdapter<PkgsPreviewAdapter.VH>(),
    DataAdapter<PreviewFilter.PkgWrap> {

    override val data = mutableListOf<PreviewFilter.PkgWrap>()

    init {
        modules.add(DataBinderMod(data))
        modules.add(SimpleVHCreatorMod { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup) :
        ModularAdapter.VH(R.layout.generator_editor_app_preview_adapter_line, parent),
        BindableVH<PreviewFilter.PkgWrap, GeneratorEditorAppPreviewAdapterLineBinding> {

        override val viewBinding = lazy { GeneratorEditorAppPreviewAdapterLineBinding.bind(itemView) }

        override val onBindData: GeneratorEditorAppPreviewAdapterLineBinding.(
            item: PreviewFilter.PkgWrap,
            payloads: List<Any>
        ) -> Unit = { item, _ ->
            val pkg = item.pkg
            val isSelected = item.isSelected
            val mode = item.mode

            name.text = item.label
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