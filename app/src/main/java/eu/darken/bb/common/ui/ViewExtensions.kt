package eu.darken.bb.common.ui

import android.content.res.TypedArray
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.StyleableRes
import eu.darken.bb.R

fun ImageView.updateExpander(dependency: View) {
    val toggleRes = if (dependency.visibility == View.VISIBLE) {
        R.drawable.ic_expand_less
    } else {
        R.drawable.ic_expand_more
    }
    setImageResource(toggleRes)
}

fun TypedArray.getStringOrRef(@StyleableRes styleRes: Int): String? {
    if (!hasValue(styleRes)) return null

    val stringId = getResourceId(styleRes, 0)
    return if (stringId != 0) {
        resources.getString(stringId)

    } else {
        getNonResourceString(styleRes)
    }
}

@DrawableRes
fun TypedArray.getDrawableRes(@StyleableRes styleRes: Int): Int? {
    if (!hasValue(styleRes)) return null

    return getResourceId(styleRes, 0)
}