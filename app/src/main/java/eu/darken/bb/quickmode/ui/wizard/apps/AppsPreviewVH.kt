package eu.darken.bb.quickmode.ui.wizard.apps

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.backup.ui.generator.editor.types.app.preview.PreviewFilter
import eu.darken.bb.databinding.QuickmodeWizardAppsPreviewItemBinding
import eu.darken.bb.quickmode.ui.wizard.common.WizardAdapter

class AppsPreviewVH(parent: ViewGroup) :
    WizardAdapter.BaseVH<AppsPreviewVH.Item, QuickmodeWizardAppsPreviewItemBinding>(
        R.layout.quickmode_wizard_apps_preview_item,
        parent
    ) {

    override val viewBinding = lazy {
        QuickmodeWizardAppsPreviewItemBinding.bind(itemView)
    }

    override val onBindData: QuickmodeWizardAppsPreviewItemBinding.(
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