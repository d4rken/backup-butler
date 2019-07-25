package eu.darken.bb.tasks.ui.editor.sources

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.backups.BackupConfig
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.DataBinderModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.SimpleVHCreator
import javax.inject.Inject


class SourcesAdapter @Inject constructor() : ModularAdapter<SourcesAdapter.VH>() {

    val data = mutableListOf<BackupConfig>()

    init {
        modules.add(DataBinderModule<BackupConfig, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup)
        : ModularAdapter.VH(R.layout.tasklist_adapter_line, parent), BindableVH<BackupConfig> {
        @BindView(R.id.icon) lateinit var icon: ImageView
        @BindView(R.id.name) lateinit var label: TextView
        @BindView(R.id.description) lateinit var description: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: BackupConfig) {
            icon.setImageResource(item.configType.iconRes)
            label.setText(item.configType.labelRes)
//            description.text = item.description
        }

    }
}