package eu.darken.bb.common

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.setupDefaults() = apply {
    layoutManager = LinearLayoutManager(context)
    itemAnimator = DefaultItemAnimator()
    addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
}