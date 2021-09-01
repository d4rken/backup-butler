package eu.darken.bb.common.lists

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

interface AutoDataAdapter<T : HasStableId> {
    val data: MutableList<T>
}

fun <X, T : HasStableId> X.update(
    newData: List<T>?,
    notify: Boolean = true,
    compareItem: (T, T) -> Boolean = { i1, i2 -> i1 == i2 },
    compareContent: (T, T) -> Boolean = { i1, i2 -> i1 == i2 }
) where X : AutoDataAdapter<T>, X : RecyclerView.Adapter<*> {

    val oldData = data.toList()
    data.clear()

    if (newData != null) data.addAll(newData)

    if (newData != null && notify) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return compareItem(oldData[oldItemPosition], newData[newItemPosition])
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return compareContent(oldData[oldItemPosition], newData[newItemPosition])
            }

            override fun getOldListSize() = oldData.size

            override fun getNewListSize() = newData.size
        })
        diff.dispatchUpdatesTo(this)
    }
}