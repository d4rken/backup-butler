package eu.darken.bb.common.root.javaroot.internal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RootHostOptions(
    val packageName: String,
    val pairingCode: String,
    val isDebug: Boolean = false,
    val waitForDebugger: Boolean = false
) : Parcelable