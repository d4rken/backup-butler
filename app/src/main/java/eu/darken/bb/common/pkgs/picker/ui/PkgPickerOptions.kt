package eu.darken.bb.common.pkgs.picker.ui

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.Keep
import eu.darken.bb.common.pkgs.Pkg
import kotlinx.parcelize.Parcelize

@Keep @Parcelize
data class PkgPickerOptions(
    val selectionLimit: Int = Int.MAX_VALUE,
    val allowSystemApps: Boolean = false,
    val allowedTypes: Set<Pkg.Type> = setOf(Pkg.Type.NORMAL),
    val payload: Bundle = Bundle()
) : Parcelable