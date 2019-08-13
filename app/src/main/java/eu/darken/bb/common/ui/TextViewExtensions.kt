package eu.darken.bb.common.ui

import android.view.View
import android.widget.TextView

fun TextView.tryTextElseHide(value: String?, hideMode: Int = View.INVISIBLE) {
    text = value
    visibility = if (value.isNullOrEmpty()) hideMode else View.VISIBLE
}