package eu.darken.bb.common.files.ui.picker.local

import android.text.format.Formatter
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.common.files.core.APathLookup
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.DataAdapter
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.SimpleVHCreatorMod
import eu.darken.bb.common.previews.FilePreviewRequest
import eu.darken.bb.common.previews.GlideApp
import eu.darken.bb.common.previews.into
import eu.darken.bb.common.ui.PreviewView
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.databinding.PathpickerLocalAdapterLineBinding
import java.text.DateFormat
import javax.inject.Inject


class APathLookupAdapter @Inject constructor() : ModularAdapter<APathLookupAdapter.VH>(), DataAdapter<APathLookup<*>> {

    override val data = mutableListOf<APathLookup<*>>()

    init {
        modules.add(DataBinderMod(data))
        modules.add(SimpleVHCreatorMod { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup) : ModularAdapter.VH(R.layout.pathpicker_local_adapter_line, parent),
        BindableVH<APathLookup<*>, PathpickerLocalAdapterLineBinding> {
        @BindView(R.id.preview_container) lateinit var previewContainer: PreviewView
        @BindView(R.id.name) lateinit var label: TextView
        @BindView(R.id.last_modified) lateinit var lastModified: TextView
        @BindView(R.id.size) lateinit var size: TextView

        private val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

        init {
            ButterKnife.bind(this, itemView)
        }

        override val viewBinding = lazy { PathpickerLocalAdapterLineBinding.bind(itemView) }

        override val onBindData: PathpickerLocalAdapterLineBinding.(
            item: APathLookup<*>,
            payloads: List<Any>
        ) -> Unit = { item, _ ->
            label.text = item.userReadableName(context)
            lastModified.text = formatter.format(item.modifiedAt)
            size.text = Formatter.formatFileSize(context, item.size)
            size.setInvisible(item.isDirectory)

            GlideApp.with(context)
                .load(FilePreviewRequest(item, context))
                .into(previewContainer)
        }

    }
}