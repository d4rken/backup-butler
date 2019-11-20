package eu.darken.bb.common.ui

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.ModularAdapter

abstract class ConfirmableActionAdapterVH<T>(parent: ViewGroup)
    : ModularAdapter.VH(R.layout.view_action_adapter_line, parent), BindableVH<Confirmable<T>> {
    @BindView(R.id.icon) lateinit var icon: ImageView
    @BindView(R.id.name) lateinit var label: TextView
    @BindView(R.id.description) lateinit var description: TextView

    init {
        ButterKnife.bind(this, itemView)
    }

    override fun bind(item: Confirmable<T>) {
        val data = item.data
        icon.setImageResource(getIcon(data))
        label.text = getLabel(data)
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

data class Confirmable<T>(
        val data: T,
        val requiredLvl: Int = 0,
        var currentLvl: Int = 0
) {

    fun guardedAction(action: (T) -> Unit) {
        if (currentLvl >= requiredLvl || requiredLvl == 0) {
            action(data)
            currentLvl = 0
        } else {
            currentLvl++
        }
    }
}