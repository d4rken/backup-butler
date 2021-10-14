package eu.darken.bb.main.ui.simple.wizard.apps

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.backup.ui.generator.editor.types.app.preview.PreviewFilter
import eu.darken.bb.databinding.SimpleModeWizardAppsPreviewItemBinding
import eu.darken.bb.main.ui.simple.wizard.common.WizardAdapter

class AppsPreviewVH(parent: ViewGroup) :
    WizardAdapter.BaseVH<AppsPreviewVH.Item, SimpleModeWizardAppsPreviewItemBinding>(
        R.layout.simple_mode_wizard_apps_preview_item,
        parent
    ) {

    override val viewBinding = lazy {
        SimpleModeWizardAppsPreviewItemBinding.bind(itemView)
    }

    override val onBindData: SimpleModeWizardAppsPreviewItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

    }

    data class Item(
        val pkgWraps: List<PreviewFilter.PkgWrap>,
        val onPreview: () -> Unit,
    ) : WizardAdapter.Item {
        override val stableId: Long = this.hashCode().toLong()
    }
}