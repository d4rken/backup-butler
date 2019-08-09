package eu.darken.bb.storage.ui.viewer.content

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.common.lists.*
import eu.darken.bb.storage.core.Storage
import java.text.DateFormat
import javax.inject.Inject

class ContentAdapter @Inject constructor()
    : ModularAdapter<ContentAdapter.VH>(), DataAdapter<Storage.Content> {

    override val data = mutableListOf<Storage.Content>()

    init {
        modules.add(DataBinderModule<Storage.Content, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup)
        : ModularAdapter.VH(R.layout.storage_viewer_contentlist_adapter_line, parent), BindableVH<Storage.Content> {

        @BindView(R.id.type_label) lateinit var typeLabel: TextView
        @BindView(R.id.type_icon) lateinit var typeIcon: ImageView
        @BindView(R.id.label) lateinit var labelText: TextView
        @BindView(R.id.repo_status) lateinit var statusText: TextView


        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: Storage.Content) {
            typeLabel.setText(item.backupSpec.backupType.labelRes)
            typeIcon.setImageResource(item.backupSpec.backupType.iconRes)

            labelText.text = item.backupSpec.getLabel(context)

            val versionCount = getQuantityString(R.plurals.x_versions, item.versioning.versions.size)
            val lastBackup = getString(
                    R.string.versions_last_backup_time_x,
                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(item.versioning.versions.first().createdAt)
            )
            @SuppressLint("SetTextI18n")
            statusText.text = "$versionCount; $lastBackup"
        }
    }

}
