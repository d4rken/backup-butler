package eu.darken.bb.common.ui

import android.view.View
import android.widget.ImageView
import eu.darken.bb.R

fun ImageView.updateExpander(dependency: View) {
    val toggleRes = if (dependency.visibility == View.VISIBLE) {
        R.drawable.ic_expand_less
    } else {
        R.drawable.ic_expand_more
    }
    setImageResource(toggleRes)
}
