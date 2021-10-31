package eu.darken.bb.task.ui.tasklist

import android.text.format.DateUtils
import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.differ.AsyncDiffer
import eu.darken.bb.common.lists.differ.DifferItem
import eu.darken.bb.common.lists.differ.HasAsyncDiffer
import eu.darken.bb.common.lists.differ.setupDiffer
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.SimpleVHCreatorMod
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.databinding.TaskListAdapterLineBinding
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.results.TaskResult
import javax.inject.Inject


class TaskListAdapter @Inject constructor() : ModularAdapter<TaskListAdapter.BackupVH>(),
    HasAsyncDiffer<TaskListAdapter.Item> {

    override val asyncDiffer: AsyncDiffer<*, Item> = setupDiffer()

    override fun getItemCount(): Int = data.size

    init {
        modules.add(DataBinderMod(data))
        modules.add(SimpleVHCreatorMod { BackupVH(it) })
    }

    data class Item(
        val task: Task,
        val lastResult: TaskResult? = null
    ) : DifferItem {
        override val stableId: Long = task.taskId.hashCode().toLong()
    }

    class BackupVH(parent: ViewGroup) : ModularAdapter.VH(R.layout.task_list_adapter_line, parent),
        BindableVH<Item, TaskListAdapterLineBinding> {

        override val viewBinding: Lazy<TaskListAdapterLineBinding> = lazy {
            TaskListAdapterLineBinding.bind(itemView)
        }

        override val onBindData: TaskListAdapterLineBinding.(
            item: Item,
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