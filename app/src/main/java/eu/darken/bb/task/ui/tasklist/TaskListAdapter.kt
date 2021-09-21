package eu.darken.bb.task.ui.tasklist

import android.text.format.DateUtils
import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.DataAdapter
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.SimpleVHCreatorMod
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.databinding.TaskListAdapterLineBinding
import eu.darken.bb.task.core.results.TaskResult
import javax.inject.Inject


class TaskListAdapter @Inject constructor() : ModularAdapter<TaskListAdapter.BackupVH>(),
    DataAdapter<TaskListFragmentVDC.TaskState> {

    override val data = mutableListOf<TaskListFragmentVDC.TaskState>()

    init {
        modules.add(DataBinderMod(data))
        modules.add(SimpleVHCreatorMod { BackupVH(it) })
    }

    override fun getItemCount(): Int = data.size

    class BackupVH(parent: ViewGroup) : ModularAdapter.VH(R.layout.task_list_adapter_line, parent),
        BindableVH<TaskListFragmentVDC.TaskState, TaskListAdapterLineBinding> {

        override val viewBinding: Lazy<TaskListAdapterLineBinding> = lazy {
            TaskListAdapterLineBinding.bind(itemView)
        }

        override val onBindData: TaskListAdapterLineBinding.(
            item: TaskListFragmentVDC.TaskState,
            payloads: List<Any>
        ) -> Unit = { item, _ ->
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