package eu.darken.bb.common.files.ui.picker.local

import android.text.format.Formatter
import android.view.ViewGroup
import androidx.core.view.isInvisible
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
import eu.darken.bb.databinding.PathPickerLocalAdapterLineBinding
import java.text.DateFormat
import javax.inject.Inject


class PathLookupAdapter @Inject constructor() : ModularAdapter<PathLookupAdapter.VH>(), DataAdapter<APathLookup<*>> {

    override val data = mutableListOf<APathLookup<*>>()

    init {
        modules.add(DataBinderMod(data))
        modules.add(SimpleVHCreatorMod { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup) : ModularAdapter.VH(R.layout.path_picker_local_adapter_line, parent),
        BindableVH<APathLookup<*>, PathPickerLocalAdapterLineBinding> {

        private val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

        override val viewBinding = lazy { PathPickerLocalAdapterLineBinding.bind(itemView) }

        override val onBindData: PathPickerLocalAdapterLineBinding.(
            item: APathLookup<*>,
            payloads: List<Any>
        ) -> Unit = { item, _ ->
            name.text = item.userReadableName(context)
            lastModified.text = formatter.format(item.modifiedAt)
            size.text = Formatter.formatFileSize(context, item.size)
            size.isInvisible = item.isDirectory

            GlideApp.with(context)
                .load(FilePreviewRequest(item, context))
                .into(previewContainer)
        }

    }
}