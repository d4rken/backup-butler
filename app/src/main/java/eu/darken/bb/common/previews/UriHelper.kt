package eu.darken.bb.common.previews

import android.net.Uri

object UriHelper {
    val APP_SCHEME = "package"

    fun fromApp(packageName: String): Uri {
        return Uri.parse("$APP_SCHEME://$packageName")
    }
}
