package eu.darken.bb.quickmode.ui.config.apps

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.backup.ui.generator.editor.types.app.preview.PreviewFilter
import eu.darken.bb.databinding.QuickmodeAppsConfigPreviewItemBinding
import eu.darken.bb.quickmode.ui.config.common.ConfigAdapter

class AppsPreviewVH(parent: ViewGroup) :
    ConfigAdapter.BaseVH<AppsPreviewVH.Item, QuickmodeAppsConfigPreviewItemBinding>(
        R.layout.quickmode_apps_config_preview_item,
        parent
    ) {

    override val viewBinding = lazy {
        QuickmodeAppsConfigPreviewItemBinding.bind(itemView)
    }

    override val onBindData: QuickmodeAppsConfigPreviewItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

    }

    data class Item(
        val pkgWraps: List<PreviewFilter.PkgWrap>,
        val onPreview: () -> Unit,
    ) : ConfigAdapter.Item {
        override val stableId: Long = this.hashCode().toLong()
    }
}