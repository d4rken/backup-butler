package eu.darken.bb.common.apps

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.pm.SharedLibraryInfo
import android.os.Build
import eu.darken.bb.common.ApiHelper

@TargetApi(Build.VERSION_CODES.O)
data class LibraryPkg(
        private val sharedLibraryInfo: SharedLibraryInfo
) : Pkg {

    override val packageName: String by lazy {
        if (versionCode == -1L) {
            sharedLibraryInfo.name
        } else {
            "${sharedLibraryInfo.name}_${versionCode}"
        }
    }

    @SuppressLint("NewApi")
    override val versionCode: Long = if (ApiHelper.hasAndroidP()) {
        sharedLibraryInfo.longVersion
    } else {
        sharedLibraryInfo.version.toLong()
    }

    override fun getLabel(ipcFunnel: IPCFunnel): String? = sharedLibraryInfo.name

    override val packageType: Pkg.Type = Pkg.Type.LIBRARY

    override fun <T> tryField(fieldName: String): T? = null

}