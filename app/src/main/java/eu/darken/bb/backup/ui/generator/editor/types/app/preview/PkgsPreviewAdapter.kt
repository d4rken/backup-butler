package eu.darken.bb.backup.ui.generator.editor.types.app.preview

import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.common.lists.*
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import eu.darken.bb.common.previews.AppPreviewRequest
import eu.darken.bb.common.previews.GlideApp
import eu.darken.bb.common.previews.into
import eu.darken.bb.common.ui.PreviewView
import javax.inject.Inject


class PkgsPreviewAdapter @Inject constructor(
    pkgOps: PkgOps
) : ModularAdapter<PkgsPreviewAdapter.VH>(), DataAdapter<AppEditorPreviewFragmentVDC.PkgWrap> {

    override val data = mutableListOf<AppEditorPreviewFragmentVDC.PkgWrap>()

    init {
        modules.add(DataBinderModule<AppEditorPreviewFragmentVDC.PkgWrap, VH>(data))
        modules.add(SimpleVHCreator { VH(it, pkgOps) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup, private val pkgOps: PkgOps) :
        ModularAdapter.VH(R.layout.generator_editor_app_preview_adapter_line, parent),
        BindableVH<AppEditorPreviewFragmentVDC.PkgWrap> {

        @BindView(R.id.preview_container) lateinit var previewContainer: PreviewView
        @BindView(R.id.name) lateinit var label: TextView
        @BindView(R.id.description) lateinit var description: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: AppEditorPreviewFragmentVDC.PkgWrap) {
            val pkg = item.pkg
            val isSelected = item.isSelected
            val mode = item.mode

            label.text = pkg.getLabel(pkgOps)
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