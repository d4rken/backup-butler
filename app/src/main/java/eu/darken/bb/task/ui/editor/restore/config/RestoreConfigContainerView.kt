package eu.darken.bb.task.ui.editor.restore.config

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import eu.darken.bb.R
import eu.darken.bb.common.ui.*
import eu.darken.bb.databinding.TaskEditorRestoreConfigsContainerViewBinding

class RestoreConfigContainerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = TaskEditorRestoreConfigsContainerViewBinding.inflate(layoutInflator, this, true)
    val subTitle by lazy { binding.cardSubtitle }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (ROOT_IDS.contains(child.id)) {
            super.addView(child, index, params)
        } else {
            binding.childContainer.addView(child, index, params)
        }
    }

    fun setConfigWrap(
        title: String,
        item: ConfigUIWrap,
    ) = binding.apply {
        cardTitle.text = title
        cardSubtitle.text = when {
            item.isDefaultItem -> getString(R.string.general_default_label)
            item.backupInfo != null -> item.backupInfo!!.spec.getLabel(context)
            else -> getString(R.string.progress_loading_label)
        }
        if (item.isCustomConfig && !item.isDefaultItem) cardTitle.append("*")

        childContainer.setGone(!item.isDefaultItem && !item.isCustomConfig)
        headerToggle.updateExpander(childContainer)
        headerContainer.setOnClickListener {
            childContainer.toggleGone()
            headerToggle.updateExpander(childContainer)
        }
    }

    companion object {
        private val ROOT_IDS = listOf(
            R.id.binding_root,
            R.id.header_container
        )
    }
}