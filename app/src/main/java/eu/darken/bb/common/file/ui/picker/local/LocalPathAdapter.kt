package eu.darken.bb.common.file.ui.picker.local

import android.text.format.Formatter
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.common.file.core.local.LocalPathCached
import eu.darken.bb.common.lists.*
import eu.darken.bb.common.previews.FilePreviewRequest
import eu.darken.bb.common.previews.GlideApp
import eu.darken.bb.common.previews.into
import eu.darken.bb.common.ui.PreviewView
import eu.darken.bb.common.ui.setInvisible
import java.text.DateFormat
import javax.inject.Inject


class LocalPathAdapter @Inject constructor()
    : ModularAdapter<LocalPathAdapter.VH>(), DataAdapter<LocalPathCached> {

    override val data = mutableListOf<LocalPathCached>()

    init {
        modules.add(DataBinderModule<LocalPathCached, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup)
        : ModularAdapter.VH(R.layout.pathpicker_local_adapter_line, parent), BindableVH<LocalPathCached> {
        @BindView(R.id.preview_container) lateinit var previewContainer: PreviewView
        @BindView(R.id.name) lateinit var label: TextView
        @BindView(R.id.last_modified) lateinit var lastModified: TextView
        @BindView(R.id.size) lateinit var size: TextView

        private val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: LocalPathCached) {
            label.text = item.userReadableName(context)
            lastModified.text = formatter.format(item.lastModified)
            size.text = Formatter.formatFileSize(context, item.size)
            size.setInvisible(item.isDirectory)

            GlideApp.with(context)
                    .load(FilePreviewRequest(item, context))
                    .into(previewContainer)
        }

    }
}