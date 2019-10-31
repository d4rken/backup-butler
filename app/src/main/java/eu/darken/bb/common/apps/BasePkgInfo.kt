package eu.darken.bb.common.apps

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PermissionInfo
import eu.darken.bb.common.ApiHelper


abstract class BasePkgInfo(internal val packageInfo: PackageInfo) : NormalPkg {
    private var labelCache: String? = null

    override val packageName: String
        get() = packageInfo.packageName

    override val applicationInfo: ApplicationInfo?
        get() = packageInfo.applicationInfo

    override val isSystemApp: Boolean
        get() = applicationInfo == null || applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM != 0

    override val versionCode: Long
        @SuppressLint("NewApi")
        get() = if (ApiHelper.hasAndroidP()) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode.toLong()
        }

    override val versionName: String?
        get() = packageInfo.versionName

    override val activities: Collection<ActivityInfo>?
        get() = if (packageInfo.activities == null) null else listOf(*packageInfo.activities)

    override val receivers: Collection<ActivityInfo>?
        get() = if (packageInfo.receivers == null) null else listOf(*packageInfo.receivers)

    override val requestedPermissions: Collection<String>?
        get() = if (packageInfo.requestedPermissions == null) null else listOf(*packageInfo.requestedPermissions)

    override val permissions: Collection<PermissionInfo>?
        get() = if (packageInfo.permissions == null) null else listOf(*packageInfo.permissions)

    override fun getLabel(ipcFunnel: IPCFunnel): String? {
        if (labelCache == null && applicationInfo != null) {
            labelCache = ipcFunnel.submit(IPCFunnel.LabelQuery(applicationInfo!!))
        }
        return labelCache
    }

    @Throws(Exception::class)
    override fun <T> tryField(fieldName: String): T? {
        return null
    }

    override fun toString(): String {
        return packageName
    }
}