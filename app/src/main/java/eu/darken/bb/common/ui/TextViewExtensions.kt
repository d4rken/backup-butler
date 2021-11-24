package eu.darken.bb.common.ui

import android.view.View
import android.widget.TextView
import androidx.annotation.PluralsRes

fun TextView.tryTextElseHide(value: String?, hideMode: Int = View.INVISIBLE) {
    text = value
    visibility = if (value.isNullOrEmpty()) hideMode else View.VISIBLE
}

fun TextView.setTextQuantity(@PluralsRes pluralsRes: Int, quantity: Int, vararg arguments: Any = arrayOf(quantity)) {
    text = resources.getQuantityString(pluralsRes, quantity, *arguments)
}

operator fun TextView.plusAssign(value: String) {
    this.append(value)
}