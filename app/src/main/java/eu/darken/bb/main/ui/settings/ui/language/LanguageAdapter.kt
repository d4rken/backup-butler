package eu.darken.bb.main.ui.settings.ui.language

import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.common.lists.*
import eu.darken.bb.main.core.LanguageEnforcer.Language
import javax.inject.Inject


class LanguageAdapter @Inject constructor() : ModularAdapter<LanguageAdapter.BackupVH>(), DataAdapter<Language> {

    override val data = mutableListOf<Language>()
    override fun getItemCount(): Int = data.size

    var currentLanguage: Language? = null

    init {
        modules.add(DataBinderModule<Language, BackupVH>(data) { _, vh, pos ->
            val item = data[pos]
            val isSelected = item == currentLanguage
            vh.bind(item, isSelected)
        })
        modules.add(SimpleVHCreator { BackupVH(it) })
    }

    fun update(data: List<Language>, current: Language) {
        currentLanguage = current
        update(data)
    }

    class BackupVH(parent: ViewGroup) : ModularAdapter.VH(R.layout.settings_ui_language_adapter_line, parent),
        BindableVH<Language> {

        @BindView(R.id.icon) lateinit var icon: ImageView
        @BindView(R.id.label) lateinit var label: TextView
        @BindView(R.id.description) lateinit var description: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: Language) {
            throw NotImplementedError()
        }

        fun bind(item: Language, isSelected: Boolean) {
            label.text = item.localeFormatted
            if (isSelected) {
                label.typeface = Typeface.DEFAULT_BOLD
            } else {
                label.typeface = Typeface.DEFAULT
            }
            description.text = item.translatorsFormatted
        }

    }
}