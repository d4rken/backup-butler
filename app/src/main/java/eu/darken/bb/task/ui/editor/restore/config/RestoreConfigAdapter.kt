package eu.darken.bb.task.ui.editor.restore.config

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.backup.core.files.FilesRestoreConfig
import eu.darken.bb.common.lists.*
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.ui.toggleGone
import eu.darken.bb.common.ui.updateExpander
import eu.darken.bb.task.ui.editor.restore.config.app.AppConfigVH
import eu.darken.bb.task.ui.editor.restore.config.files.FileConfigVH
import javax.inject.Inject


class RestoreConfigAdapter @Inject constructor()
    : ModularAdapter<RestoreConfigAdapter.BaseVH>(), AsyncAutoDataAdapter<ConfigUIWrap> {

    override val asyncDiffer: AsyncDiffer<ConfigUIWrap> = AsyncDiffer(
            this,
            compareItem = { i1, i2 -> i1.stableId == i2.stableId },
            compareContent = { i1, i2 -> i1.config == i2.config }
    )

    override fun getItemId(position: Int): Long = data[position].stableId

    override fun getItemCount(): Int = data.size

    init {
        setHasStableIds(true)
        modules.add(DataBinderModule<ConfigUIWrap, BaseVH>(data))
        modules.add(TypedVHCreator(0, { data[it].config is AppRestoreConfig }) { AppConfigVH(it) })
        modules.add(TypedVHCreator(1, { data[it].config is FilesRestoreConfig }) { FileConfigVH(it) })
    }

    abstract class BaseVH(@LayoutRes layoutRes: Int, parent: ViewGroup)
        : ModularAdapter.VH(layoutRes, parent), BindableVH<ConfigUIWrap> {

        private val defaultTag = getString(R.string.general_default_label)
        abstract val title: String

        @BindView(R.id.card_title) lateinit var cardTitle: TextView
        @BindView(R.id.card_subtitle) lateinit var cardSubTitle: TextView
        @BindView(R.id.header_container) lateinit var headerContainer: ViewGroup
        @BindView(R.id.header_toggle) lateinit var headerToggle: ImageView
        @BindView(R.id.options_container) lateinit var optionsContainer: ViewGroup

        @CallSuper
        override fun bind(item: ConfigUIWrap) {
            cardTitle.text = title
            cardSubTitle.text = when {
                item.isDefaultItem -> defaultTag
                item.backupInfo != null -> item.backupInfo.spec.getLabel(context)
                else -> getString(R.string.progress_loading_label)
            }
            if (item.isCustomConfig && !item.isDefaultItem) cardTitle.append("*")

            optionsContainer.setGone(!item.isDefaultItem && !item.isCustomConfig)
            headerToggle.updateExpander(optionsContainer)
            headerContainer.setOnClickListener {
                optionsContainer.toggleGone()
                headerToggle.updateExpander(optionsContainer)
            }
        }
    }

}