package eu.darken.bb.common

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment


@ColorInt
fun Context.getColorForAttr(@AttrRes attrId: Int): Int {
    var typedArray: TypedArray? = null
    try {
        typedArray = this.theme.obtainStyledAttributes(intArrayOf(attrId))
        return typedArray.getColor(0, 0)
    } finally {
        typedArray?.recycle()
    }
}

@ColorInt
fun Fragment.getColorForAttr(@AttrRes attrId: Int): Int = requireContext().getColorForAttr(attrId)

@ColorInt
fun Context.getCompatColor(@ColorRes attrId: Int): Int {
    return ContextCompat.getColor(this, attrId)
}

@ColorInt
fun Fragment.getCompatColor(@ColorRes attrId: Int): Int = requireContext().getCompatColor(attrId)
