package eu.darken.bb.common.lists.differ

import androidx.recyclerview.widget.RecyclerView
import eu.darken.bb.common.lists.modular.ModularAdapter


fun <X, T> X.update(newData: List<T>?, notify: Boolean = true)
    where X : HasAsyncDiffer<T>, X : RecyclerView.Adapter<*> {

    if (notify) asyncDiffer.submitUpdate(newData ?: emptyList())
}

fun <A, T : DifferItem> A.setupDiffer(): AsyncDiffer<A, T>
    where A : HasAsyncDiffer<T>, A : ModularAdapter<*> =
    AsyncDiffer(this)
