package eu.darken.bb.storage.ui.viewer.item

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.lists.*
import java.text.DateFormat
import javax.inject.Inject

class StorageItemAdapter @Inject constructor()
    : ModularAdapter<StorageItemAdapter.VH>(), DataAdapter<BackupSpec.Info> {

    override val data = mutableListOf<BackupSpec.Info>()

    init {
        modules.add(DataBinderModule<BackupSpec.Info, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup)
        : ModularAdapter.VH(R.layout.storage_viewer_contentlist_adapter_line, parent), BindableVH<BackupSpec.Info> {

        @BindView(R.id.type_label) lateinit var typeLabel: TextView
        @BindView(R.id.type_icon) lateinit var typeIcon: ImageView
        @BindView(R.id.label) lateinit var labelText: TextView
        @BindView(R.id.repo_status) lateinit var statusText: TextView

        private val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(info: BackupSpec.Info) {
            typeLabel.setText(info.backupSpec.backupType.labelRes)
            typeIcon.setImageResource(info.backupSpec.backupType.iconRes)

            labelText.text = info.backupSpec.getLabel(context)

            val versionCount = getQuantityString(R.plurals.x_versions, info.backups.size)
            val lastBackup = getString(
                    R.string.versions_last_backup_time_x,
                    formatter.format(info.backups.first().createdAt)
            )
            @SuppressLint("SetTextI18n")
            statusText.text = "$versionCount; $lastBackup"
        }
    }

}
