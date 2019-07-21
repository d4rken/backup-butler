package eu.darken.bb.tasks.ui.tasklist

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.common.BaseVH
import eu.darken.bb.common.BindableVH
import eu.darken.bb.tasks.core.BackupTask
import javax.inject.Inject

class TaskListAdapter @Inject constructor() : RecyclerView.Adapter<TaskListAdapter.BackupVH>() {

    var data = listOf<BackupTask>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackupVH = BackupVH(parent)

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: BackupVH, position: Int) = holder.bind(data[position])


    class BackupVH(parent: ViewGroup) : BaseVH(R.layout.tasklist_adapter_line, parent), BindableVH<BackupTask> {
        @BindView(R.id.name) lateinit var taskName: TextView
        @BindView(R.id.primary_description) lateinit var primary: TextView
        @BindView(R.id.type_icon) lateinit var statusIcon: ImageView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: BackupTask) {
            taskName.text = item.taskName
            primary.text = item.id.toString()
            statusIcon.setImageResource(R.drawable.ic_error_outline)
            statusIcon.setColorFilter(getColor(R.color.colorError))
        }

    }
}