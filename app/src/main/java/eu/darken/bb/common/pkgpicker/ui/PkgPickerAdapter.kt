package eu.darken.bb.common.pkgpicker.ui

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.common.lists.*
import eu.darken.bb.common.lists.differ.AsyncDiffer
import eu.darken.bb.common.lists.differ.DifferItem
import eu.darken.bb.common.lists.differ.HasAsyncDiffer
import eu.darken.bb.common.lists.differ.setupDiffer
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.SimpleVHCreatorMod
import eu.darken.bb.common.pkgs.NormalPkg
import eu.darken.bb.common.previews.AppPreviewRequest
import eu.darken.bb.common.previews.GlideApp
import eu.darken.bb.common.previews.into
import eu.darken.bb.databinding.PkgPickerAdapterLineBinding
import javax.inject.Inject

class PkgPickerAdapter @Inject constructor() : ModularAdapter<PkgPickerAdapter.VH>(),
    HasAsyncDiffer<PkgPickerAdapter.Item> {

    override val asyncDiffer: AsyncDiffer<PkgPickerAdapter, Item> = setupDiffer()

    override fun getItemCount(): Int = data.size

    init {
        modules.add(DataBinderMod(data))
        modules.add(SimpleVHCreatorMod { VH(it) })
    }

    data class Item(
        val pkg: NormalPkg,
        val label: String,
        val isSelected: Boolean
    ) : DifferItem {
        override val stableId: Long
            get() = pkg.packageName.hashCode().toLong()
    }

    class VH(parent: ViewGroup) : ModularAdapter.VH(R.layout.pkg_picker_adapter_line, parent),
        BindableVH<Item, PkgPickerAdapterLineBinding> {

        override val viewBinding = lazy { PkgPickerAdapterLineBinding.bind(itemView) }

        override val onBindData: PkgPickerAdapterLineBinding.(
            item: Item,
            payloads: List<Any>
        ) -> Unit = onBindData@{ item, _ ->
            name.text = item.label
            description.text = item.pkg.packageName
            checkbox.isChecked = item.isSelected

            GlideApp.with(context)
                .load(AppPreviewRequest(item.pkg, context))
                .into(previewContainer)
        }
    }

}
