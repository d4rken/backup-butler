package eu.darken.bb.task.ui.editor.restore.sources

import android.annotation.SuppressLint
import android.text.format.Formatter
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import butterknife.BindView
import butterknife.ButterKnife
import com.airbnb.lottie.LottieAnimationView
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.getColorForAttr
import eu.darken.bb.common.lists.*
import eu.darken.bb.common.tryLocalizedErrorMessage
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.storage.core.Storage
import java.text.DateFormat
import javax.inject.Inject


class RestoreSourcesAdapter @Inject constructor()
    : ModularAdapter<RestoreSourcesAdapter.BaseVH>(), DataAdapter<Any> {

    override val data = mutableListOf<Any>()

    init {
        modules.add(DataBinderModule<Any, BaseVH>(data))
        modules.add(TypedVHCreator(0, { data[it] is Storage.InfoOpt }) { StorageVH(it) })
        modules.add(TypedVHCreator(1, { data[it] is BackupSpec.InfoOpt }) { SpecVH(it) })
        modules.add(TypedVHCreator(2, { data[it] is Backup.InfoOpt }) { BackupVH(it) })
    }

    override fun getItemCount(): Int = data.size

    abstract class BaseVH(@LayoutRes layoutRes: Int, parent: ViewGroup)
        : ModularAdapter.VH(layoutRes, parent), BindableVH<Any>

    class StorageVH(parent: ViewGroup)
        : BaseVH(R.layout.task_editor_restore_sources_adapter_line_storage, parent) {

        @BindView(R.id.type_label) lateinit var typeLabel: TextView
        @BindView(R.id.type_icon) lateinit var typeIcon: ImageView
        @BindView(R.id.label) lateinit var labelText: TextView
        @BindView(R.id.repo_status) lateinit var statusText: TextView
        @BindView(R.id.loading_animation) lateinit var loadingAnimation: LottieAnimationView

        init {
            ButterKnife.bind(this, itemView)
        }

        // FIXME same as storage > list > adapter?
        override fun bind(item: Any) {
            item as Storage.InfoOpt
            if (item.info == null) {
                typeLabel.setText(R.string.label_unknown)
                typeIcon.setColorFilter(getColor(R.color.colorError))
                typeIcon.setImageResource(R.drawable.ic_error_outline)

                labelText.text = "?"

                statusText.text = getString(R.string.error_message_cant_find_x, item.storageId)
                return
            }

            val info = item.info
            typeLabel.setText(info.storageType.labelRes)
            typeIcon.setImageResource(info.storageType.iconRes)
            typeIcon.setColorFilter(context.getColorForAttr(android.R.attr.textColorSecondary))

            if (info.config != null) {
                labelText.text = info.config.label
            } else {
                labelText.setText(R.string.progress_loading_label)
            }

            when {
                info.error != null -> {
                    statusText.setTextColor(getColor(R.color.colorError))
                    statusText.text = info.error.tryLocalizedErrorMessage(context)
                }
                info.status != null -> {
                    statusText.setTextColor(context.getColorForAttr(android.R.attr.textColorSecondary))
                    @SuppressLint("SetTextI18n")
                    statusText.text = "${getQuantityString(R.plurals.x_items, info.status.itemCount)}; ${Formatter.formatFileSize(context, info.status.totalSize)}"
                    if (info.status.isReadOnly) statusText.append("; " + getString(R.string.read_only_label))
                }
                else -> {
                    statusText.setTextColor(context.getColorForAttr(android.R.attr.textColorSecondary))
                    statusText.text = null
                }
            }

            loadingAnimation.setGone(info.config != null && info.status != null)
        }
    }

    class SpecVH(parent: ViewGroup)
        : BaseVH(R.layout.task_editor_restore_sources_adapter_line_backupspec, parent) {

        @BindView(R.id.type_label) lateinit var typeLabel: TextView
        @BindView(R.id.type_icon) lateinit var typeIcon: ImageView
        @BindView(R.id.label) lateinit var labelText: TextView
        @BindView(R.id.repo_status) lateinit var statusText: TextView

        private val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: Any) {
            item as BackupSpec.InfoOpt

            if (item.info != null) {
                val info = item.info
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
            } else {

            }

        }

    }

    class BackupVH(parent: ViewGroup)
        : BaseVH(R.layout.task_editor_restore_sources_adapter_line_backupversion, parent) {

        @BindView(R.id.type_label) lateinit var typeLabel: TextView
        @BindView(R.id.type_icon) lateinit var typeIcon: ImageView
        @BindView(R.id.label) lateinit var labelText: TextView
        @BindView(R.id.repo_status) lateinit var statusText: TextView

        private val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: Any) {
            item as Backup.InfoOpt

            if (item.info != null) {
                val info = item.info
                typeLabel.setText(info.spec.backupType.labelRes)
                typeIcon.setImageResource(info.spec.backupType.iconRes)

                labelText.text = info.spec.getLabel(context)

                val itemCount = getQuantityString(R.plurals.x_items, -1)
                val backupDate = formatter.format(info.metaData.createdAt)

                @SuppressLint("SetTextI18n")
                statusText.text = "$itemCount; $backupDate"
            } else {

            }
        }

    }
}