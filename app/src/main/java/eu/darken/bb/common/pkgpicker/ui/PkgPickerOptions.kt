package eu.darken.bb.common.pkgpicker.ui

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep @Parcelize
data class PkgPickerOptions(
    val selectionLimit: Int = Int.MAX_VALUE,
    val payload: Bundle = Bundle()
) : Parcelable