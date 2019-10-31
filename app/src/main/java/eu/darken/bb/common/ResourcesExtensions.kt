package eu.darken.bb.common

import android.content.res.Resources
import androidx.annotation.PluralsRes

fun Resources.getCountString(@PluralsRes stringRes: Int, quantity: Int): String =
        getQuantityString(stringRes, quantity, quantity)