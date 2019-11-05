package eu.darken.bb.task.ui.editor.restore.sources

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.airbnb.lottie.LottieAnimationView
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.lists.*
import eu.darken.bb.common.tryLocalizedErrorMessage
import eu.darken.bb.common.ui.setInvisible
import java.text.DateFormat
import javax.inject.Inject


class BackupAdapter @Inject constructor()
    : ModularAdapter<BackupAdapter.VH>(), DataAdapter<Backup.InfoOpt> {

    override val data = mutableListOf<Backup.InfoOpt>()

    init {
        modules.add(DataBinderModule<Backup.InfoOpt, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup)
        : ModularAdapter.VH(R.layout.task_editor_restore_sources_adapter_line_backup, parent), BindableVH<Backup.InfoOpt> {

        @BindView(R.id.type_label) lateinit var typeLabel: TextView
        @BindView(R.id.type_icon) lateinit var typeIcon: ImageView
        @BindView(R.id.loading_animation) lateinit var loadingAnimation: LottieAnimationView
        @BindView(R.id.label) lateinit var labelText: TextView
        @BindView(R.id.repo_status) lateinit var statusText: TextView

        private val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: Backup.InfoOpt) {
            when {
                item.error != null -> {
                    typeLabel.setText(R.string.general_unknown_label)
                    typeIcon.setImageResource(R.drawable.ic_error_outline)

                    labelText.setText(R.string.general_error_label)

                    statusText.text = item.error.tryLocalizedErrorMessage(context)
                }
                item.info != null -> {
                    typeLabel.setText(item.info.backupType.labelRes)
                    typeIcon.setImageResource(item.info.backupType.iconRes)

                    labelText.text = item.info.spec.getLabel(context)

                    val itemCount = getQuantityString(R.plurals.x_items, -1)
                    val backupDate = formatter.format(item.info.metaData.createdAt)

                    @SuppressLint("SetTextI18n")
                    statusText.text = "$itemCount; $backupDate"
                }
                else -> {
                    typeLabel.setText(R.string.general_unknown_label)
                    labelText.setText(R.string.progress_loading_label)
                    statusText.text = ""
                }
            }
            loadingAnimation.setInvisible(item.isFinished)
            typeIcon.setInvisible(!item.isFinished)
        }
    }
}