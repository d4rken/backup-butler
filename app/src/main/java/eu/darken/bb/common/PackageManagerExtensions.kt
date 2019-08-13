package eu.darken.bb.common

import android.content.pm.PackageManager

fun PackageManager.tryGetAppLabel(packageName: String): String {
    return try {
        getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES).loadLabel(this).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        null
    } ?: packageName
}