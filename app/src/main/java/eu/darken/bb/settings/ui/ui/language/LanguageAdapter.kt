package eu.darken.bb.settings.ui.ui.language

import android.graphics.Typeface
import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.DataAdapter
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.SimpleVHCreatorMod
import eu.darken.bb.databinding.SettingsUiLanguageAdapterLineBinding
import javax.inject.Inject


class LanguageAdapter @Inject constructor() : ModularAdapter<LanguageAdapter.BackupVH>(), DataAdapter<LanguageItem> {

    override val data = mutableListOf<LanguageItem>()
    override fun getItemCount(): Int = data.size

    init {
        modules.add(DataBinderMod(data))
        modules.add(SimpleVHCreatorMod { BackupVH(it) })
    }

    class BackupVH(parent: ViewGroup) : ModularAdapter.VH(R.layout.settings_ui_language_adapter_line, parent),
        BindableVH<LanguageItem, SettingsUiLanguageAdapterLineBinding> {

        override val viewBinding: Lazy<SettingsUiLanguageAdapterLineBinding> = lazy {
            SettingsUiLanguageAdapterLineBinding.bind(itemView)
        }

        override val onBindData: SettingsUiLanguageAdapterLineBinding.(
            item: LanguageItem,
            payloads: List<Any>
        ) -> Unit = { item, _ ->
            label.text = item.language.localeFormatted
            if (item.isSelected) {
                label.typeface = Typeface.DEFAULT_BOLD
            } else {
                label.typeface = Typeface.DEFAULT
            }
            description.text = item.language.translatorsFormatted
        }
    }
}