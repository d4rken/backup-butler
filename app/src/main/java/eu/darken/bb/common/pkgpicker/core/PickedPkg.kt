package eu.darken.bb.common.pkgpicker.core

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PickedPkg(
    val pkg: String
) : Parcelable
