package eu.darken.bb.task.ui.editor.common.requirements

import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.LayoutRes
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.common.lists.*
import eu.darken.bb.task.core.common.requirements.Requirement
import javax.inject.Inject


class RequirementsAdapter @Inject constructor()
    : ModularAdapter<RequirementsAdapter.BaseVH>(), DataAdapter<Requirement> {

    override val data = mutableListOf<Requirement>()

    init {
        modules.add(DataBinderModule<Requirement, BaseVH>(data))
        modules.add(TypedVHCreator(0, { data[it] is Requirement.Permission }) { PermissionVH(it) })
    }

    override fun getItemCount(): Int = data.size

    abstract class BaseVH(@LayoutRes layoutId: Int, parent: ViewGroup)
        : ModularAdapter.VH(layoutId, parent), BindableVH<Requirement>

    class PermissionVH(parent: ViewGroup)
        : BaseVH(R.layout.task_editor_requirement_adapter_line, parent) {

        @BindView(R.id.label) lateinit var labelText: TextView
        @BindView(R.id.description) lateinit var descriptionText: TextView
        @BindView(R.id.requirement_main_action) lateinit var mainAction: Button

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: Requirement) {
            labelText.text = item.label.get(context)
            descriptionText.text = item.description.get(context)
            mainAction.text = item.mainActionLabel.get(context)
            mainAction.setOnClickListener { itemView.performClick() }
        }
    }
}