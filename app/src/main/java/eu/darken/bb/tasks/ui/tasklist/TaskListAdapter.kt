package eu.darken.bb.tasks.ui.tasklist

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.DataBinderModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.SimpleVHCreator
import eu.darken.bb.tasks.core.BackupTask
import javax.inject.Inject


class TaskListAdapter @Inject constructor() : ModularAdapter<TaskListAdapter.BackupVH>() {

    val data = mutableListOf<BackupTask>()

    init {
        modules.add(DataBinderModule<BackupTask, BackupVH>(data))
        modules.add(SimpleVHCreator { BackupVH(it) })
    }

    override fun getItemCount(): Int = data.size

    class BackupVH(parent: ViewGroup)
        : ModularAdapter.VH(R.layout.tasklist_adapter_line, parent), BindableVH<BackupTask> {
        @BindView(R.id.name) lateinit var taskName: TextView
        @BindView(R.id.primary_description) lateinit var primary: TextView
        @BindView(R.id.type_icon) lateinit var statusIcon: ImageView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: BackupTask) {
            taskName.text = item.taskName
            primary.text = item.taskId.toString()
            statusIcon.setImageResource(R.drawable.ic_error_outline)
            statusIcon.setColorFilter(getColor(R.color.colorError))
        }

    }
}