package eu.darken.bb.common

import android.os.Build

// Can't be const because that prevents them from being mocked in tests
@Suppress("MayBeConstant")
object BuildWrap {

    val VERSION = VersionWrap

    object VersionWrap {
        val SDK_INT = Build.VERSION.SDK_INT
    }
}

fun BuildWrap.hasAPILevel(level: Int): Boolean = BuildWrap.VERSION.SDK_INT >= level
