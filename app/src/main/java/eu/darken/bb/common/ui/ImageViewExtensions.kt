package eu.darken.bb.common.ui

import android.view.View

fun View.setGone(gone: Boolean) {
    visibility = if (gone) View.GONE else View.VISIBLE
}

fun View.setInvisible(invisible: Boolean) {
    visibility = if (invisible) View.INVISIBLE else View.VISIBLE
}

fun View.toggleGone() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}