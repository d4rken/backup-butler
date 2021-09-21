package eu.darken.bb.task.ui.editor.common.requirements

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import eu.darken.bb.R
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.DataAdapter
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.TypedVHCreatorMod
import eu.darken.bb.databinding.TaskEditorRequirementAdapterLineBinding
import eu.darken.bb.task.core.common.requirements.Requirement
import javax.inject.Inject


class RequirementsAdapter @Inject constructor() : ModularAdapter<RequirementsAdapter.BaseVH>(),
    DataAdapter<Requirement> {

    override val data = mutableListOf<Requirement>()

    init {
        modules.add(DataBinderMod(data))
        modules.add(TypedVHCreatorMod({ data[it] is Requirement.Permission }) { PermissionVH(it) })
    }

    override fun getItemCount(): Int = data.size

    abstract class BaseVH(@LayoutRes layoutId: Int, parent: ViewGroup) : ModularAdapter.VH(layoutId, parent),
        BindableVH<Requirement, TaskEditorRequirementAdapterLineBinding>

    class PermissionVH(parent: ViewGroup) : BaseVH(R.layout.task_editor_requirement_adapter_line, parent) {

        override val viewBinding: Lazy<TaskEditorRequirementAdapterLineBinding> = lazy {
            TaskEditorRequirementAdapterLineBinding.bind(itemView)
        }

        override val onBindData: TaskEditorRequirementAdapterLineBinding.(
            item: Requirement,
            payloads: List<Any>
        ) -> Unit = { item, _ ->
            label.text = item.label.get(context)
            description.text = item.description.get(context)
            requirementMainAction.text = item.mainActionLabel.get(context)
            requirementMainAction.setOnClickListener { itemView.performClick() }
        }
    }
}