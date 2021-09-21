package eu.darken.bb.storage.ui.viewer.item

import android.annotation.SuppressLint
import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.DataAdapter
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.SimpleVHCreatorMod
import eu.darken.bb.databinding.StorageViewerContentlistAdapterLineBinding
import java.text.DateFormat
import javax.inject.Inject

class StorageItemAdapter @Inject constructor() : ModularAdapter<StorageItemAdapter.VH>(), DataAdapter<BackupSpec.Info> {

    override val data = mutableListOf<BackupSpec.Info>()

    init {
        modules.add(DataBinderMod(data))
        modules.add(SimpleVHCreatorMod { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup) : ModularAdapter.VH(R.layout.storage_viewer_contentlist_adapter_line, parent),
        BindableVH<BackupSpec.Info, StorageViewerContentlistAdapterLineBinding> {

        private val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

        override val viewBinding: Lazy<StorageViewerContentlistAdapterLineBinding> = lazy {
            StorageViewerContentlistAdapterLineBinding.bind(itemView)
        }

        override val onBindData: StorageViewerContentlistAdapterLineBinding.(
            item: BackupSpec.Info,
            payloads: List<Any>
        ) -> Unit = { item, _ ->
            typeLabel.setText(item.backupSpec.backupType.labelRes)
            typeIcon.setImageResource(item.backupSpec.backupType.iconRes)

            label.text = item.backupSpec.getLabel(context)

            val versionCount = getQuantityString(R.plurals.x_versions, item.backups.size)
            val lastBackupText = getString(
                R.string.general_last_backup_time_x,
                formatter.format(item.backups.maxByOrNull { it.createdAt }!!.createdAt)
            )
            @SuppressLint("SetTextI18n")
            repoStatus.text = "$versionCount; $lastBackupText"
        }
    }

}
