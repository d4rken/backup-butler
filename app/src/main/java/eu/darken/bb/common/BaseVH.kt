package eu.darken.bb.common

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

abstract class BaseVH(@LayoutRes layoutRes: Int, parent: ViewGroup)
    : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)) {

    val context: Context = parent.context

    fun getColor(@ColorRes colorRes: Int): Int = ContextCompat.getColor(context, colorRes)

}