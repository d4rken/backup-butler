package eu.darken.bb.task.ui.editor.restore.sources

import android.annotation.SuppressLint
import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.errors.localized
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.DataAdapter
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.SimpleVHCreatorMod
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.databinding.TaskEditorRestoreSourcesAdapterLineBackupBinding
import java.text.DateFormat
import javax.inject.Inject


class BackupAdapter @Inject constructor() : ModularAdapter<BackupAdapter.VH>(), DataAdapter<Backup.InfoOpt> {

    override val data = mutableListOf<Backup.InfoOpt>()

    init {
        modules.add(DataBinderMod(data))
        modules.add(SimpleVHCreatorMod { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup) : ModularAdapter.VH(R.layout.task_editor_restore_sources_adapter_line_backup, parent),
        BindableVH<Backup.InfoOpt, TaskEditorRestoreSourcesAdapterLineBackupBinding> {

        private val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

        override val viewBinding: Lazy<TaskEditorRestoreSourcesAdapterLineBackupBinding> = lazy {
            TaskEditorRestoreSourcesAdapterLineBackupBinding.bind(itemView)
        }
        override val onBindData: TaskEditorRestoreSourcesAdapterLineBackupBinding.(
            item: Backup.InfoOpt,
            payloads: List<Any>
        ) -> Unit = { item, _ ->
            when {
                item.error != null -> {
                    typeLabel.setText(R.string.general_unknown_label)
                    typeIcon.setImageResource(R.drawable.ic_error_outline)

                    item.error.localized(context).let {
                        label.text = it.label
                        repoStatus.text = it.description
                    }
                }
                item.info != null -> {
                    typeLabel.setText(item.info.backupType.labelRes)
                    typeIcon.setImageResource(item.info.backupType.iconRes)

                    label.text = item.info.spec.getLabel(context)

                    val itemCount = getQuantityString(R.plurals.x_items, -1)
                    val backupDate = formatter.format(item.info.metaData.createdAt)

                    @SuppressLint("SetTextI18n")
                    repoStatus.text = "$itemCount; $backupDate"
                }
                else -> {
                    typeLabel.setText(R.string.general_unknown_label)
                    label.setText(R.string.progress_loading_label)
                    repoStatus.text = ""
                }
            }
            loadingAnimation.setInvisible(item.isFinished)
            typeIcon.setInvisible(!item.isFinished)
        }
    }
}