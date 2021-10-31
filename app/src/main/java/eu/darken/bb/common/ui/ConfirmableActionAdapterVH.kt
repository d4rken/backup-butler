package eu.darken.bb.common.ui

import android.view.ViewGroup
import androidx.annotation.DrawableRes
import eu.darken.bb.R
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.differ.DifferItem
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.databinding.ViewActionAdapterLineBinding

abstract class ConfirmableActionAdapterVH<T : DifferItem>(parent: ViewGroup) :
    ModularAdapter.VH(R.layout.view_action_adapter_line, parent),
    BindableVH<Confirmable<T>, ViewActionAdapterLineBinding> {

    override val viewBinding = lazy { ViewActionAdapterLineBinding.bind(itemView) }
    override val onBindData: ViewActionAdapterLineBinding.(
        item: Confirmable<T>,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        val data = item.data
        icon.setImageResource(getIcon(data))
        name.text = getLabel(data)
        when {
            item.currentLvl > 1 -> {
                description.setText(R.string.general_confirmation_confirmation_label)
                itemView.setBackgroundColor(getColorForAttr(R.attr.colorError))
            }
            item.currentLvl == 1 -> {
                description.setText(R.string.general_confirmation_label)
                itemView.setBackgroundColor(getColorForAttr(R.attr.colorError))
            }
            else -> {
                description.text = getDesc(data)
                itemView.setBackgroundColor(getColorForAttr(R.attr.colorPrimary))
            }
        }
        description.setGone(description.text.isNullOrEmpty())
    }

    @DrawableRes
    abstract fun getIcon(item: T): Int

    abstract fun getLabel(item: T): String

    open fun getDesc(item: T): String? = null

}

data class Confirmable<T : DifferItem>(
    val data: T,
    val requiredLvl: Int = 0,
    var currentLvl: Int = 0
) : DifferItem {

    fun guardedAction(action: (T) -> Unit) {
        if (currentLvl >= requiredLvl || requiredLvl == 0) {
            action(data)
            currentLvl = 0
        } else {
            currentLvl++
        }
    }

    override val stableId: Long
        get() = data.stableId
}