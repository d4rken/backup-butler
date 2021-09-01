package eu.darken.bb.common.lists

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import eu.darken.bb.common.getColorForAttr

abstract class BaseAdapter<T : BaseAdapter.VH> : RecyclerView.Adapter<T>() {

    @CallSuper
    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): T {
        return onCreateBaseVH(parent, viewType)
    }

    abstract fun onCreateBaseVH(parent: ViewGroup, viewType: Int): T

    @CallSuper
    final override fun onBindViewHolder(holder: T, position: Int) {
        onBindBaseVH(holder, position)
    }

    abstract fun onBindBaseVH(holder: T, position: Int)

    abstract class VH(@LayoutRes layoutRes: Int, parent: ViewGroup) :
        RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)) {

        val context: Context = parent.context

        fun getColor(@ColorRes colorRes: Int): Int = ContextCompat.getColor(context, colorRes)

        fun getColorForAttr(@AttrRes attrRes: Int): Int = context.getColorForAttr(attrRes)

        fun getString(@StringRes stringRes: Int, vararg args: Any): String = context.getString(stringRes, *args)

        fun getQuantityString(@PluralsRes pluralRes: Int, quantity: Int, vararg args: Any): String =
            context.resources.getQuantityString(pluralRes, quantity, *args)

        fun getQuantityString(@PluralsRes pluralRes: Int, quantity: Int): String =
            context.resources.getQuantityString(pluralRes, quantity, quantity)

    }
}