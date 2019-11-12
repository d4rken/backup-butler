package eu.darken.bb.common.file.ui.picker.local

import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.common.file.core.local.LocalPath
import eu.darken.bb.common.lists.*
import eu.darken.bb.common.previews.FilePreviewRequest
import eu.darken.bb.common.previews.GlideApp
import eu.darken.bb.common.previews.into
import eu.darken.bb.common.ui.PreviewView
import javax.inject.Inject


class LocalPathAdapter @Inject constructor()
    : ModularAdapter<LocalPathAdapter.VH>(), DataAdapter<LocalPath> {

    override val data = mutableListOf<LocalPath>()

    init {
        modules.add(DataBinderModule<LocalPath, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup)
        : ModularAdapter.VH(R.layout.pathpicker_local_adapter_line, parent), BindableVH<LocalPath> {
        @BindView(R.id.preview_container) lateinit var previewContainer: PreviewView
        @BindView(R.id.name) lateinit var label: TextView
        @BindView(R.id.description) lateinit var description: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: LocalPath) {
            label.text = item.userReadablePath(context)

            GlideApp.with(context)
                    .load(FilePreviewRequest(item, context))
                    .into(previewContainer)
        }

    }
}