package eu.darken.bb.common.lists

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

interface AsyncAutoDataAdapter<T : HasStableId> {

    val data: List<T>
        get() = asyncDiffer.currentList

    val asyncDiffer: AsyncDiffer<T>

}

fun <X, T> X.update(newData: List<T>?, notify: Boolean = true)
        where X : AsyncAutoDataAdapter<T>, X : RecyclerView.Adapter<*> {

    if (notify) asyncDiffer.submitUpdate(newData ?: emptyList())
}

class AsyncDiffer<T>(
        adapter: RecyclerView.Adapter<*>,
        compareItem: (T, T) -> Boolean = { i1, i2 -> i1 == i2 },
        compareContent: (T, T) -> Boolean = { i1, i2 -> i1 == i2 }
) {
    private val callback = object : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return compareItem(oldItem, newItem)
        }

        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return compareContent(oldItem, newItem)
        }
    }

    private val listDiffer = AsyncListDiffer(adapter, callback)
    private val internalList = mutableListOf<T>()
    val currentList: List<T>
        get() = synchronized(internalList) { internalList }

    fun submitUpdate(newData: List<T>) {
        listDiffer.submitList(newData) {
            synchronized(internalList) {
                internalList.clear()
                internalList.addAll(newData)
            }
        }
    }
}