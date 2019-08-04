package eu.darken.bb.common.lists

interface DataAdapter<T> {
    val data: MutableList<T>
}

fun <X, T> X.update(newData: List<T>, notify: Boolean = true) where X : DataAdapter<T>, X : ModularAdapter<*> {
    data.clear()
    data.addAll(newData)
    if (notify) notifyDataSetChanged()
}