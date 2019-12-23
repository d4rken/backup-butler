package eu.darken.bb.task.ui.tasklist

import android.text.format.DateUtils
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.common.lists.*
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.task.core.results.TaskResult
import javax.inject.Inject


class TaskListAdapter @Inject constructor()
    : ModularAdapter<TaskListAdapter.BackupVH>(), DataAdapter<TaskListFragmentVDC.TaskState> {

    override val data = mutableListOf<TaskListFragmentVDC.TaskState>()

    init {
        modules.add(DataBinderModule<TaskListFragmentVDC.TaskState, BackupVH>(data))
        modules.add(SimpleVHCreator { BackupVH(it) })
    }

    override fun getItemCount(): Int = data.size

    class BackupVH(parent: ViewGroup)
        : ModularAdapter.VH(R.layout.task_list_adapter_line, parent), BindableVH<TaskListFragmentVDC.TaskState> {

        @BindView(R.id.type_label) lateinit var typeLabel: TextView
        @BindView(R.id.type_icon) lateinit var typeIcon: ImageView
        @BindView(R.id.task_label) lateinit var taskLabel: TextView
        @BindView(R.id.primary) lateinit var primary: TextView
        @BindView(R.id.status_icon) lateinit var statusIcon: ImageView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: TaskListFragmentVDC.TaskState) {
            val task = item.task
            val lastResult = item.lastResult
            typeLabel.setText(task.taskType.labelRes)
            typeIcon.setImageResource(task.taskType.iconRes)

            taskLabel.text = task.label
            primary.text = task.getDescription(context)

            if (lastResult != null) {
                primary.append(" | " + DateUtils.getRelativeTimeSpanString(lastResult.startedAt.time + lastResult.duration))

                when (lastResult.state) {
                    TaskResult.State.SUCCESS -> {
                        statusIcon.setImageResource(R.drawable.ic_check_circle)
                        statusIcon.setColorFilter(getColor(R.color.colorSecondary))
                    }
                    TaskResult.State.ERROR -> {
                        statusIcon.setImageResource(R.drawable.ic_error_outline)
                        statusIcon.setColorFilter(getColor(R.color.colorError))
                    }
                }

            }
            statusIcon.setGone(lastResult == null)
        }

    }
}