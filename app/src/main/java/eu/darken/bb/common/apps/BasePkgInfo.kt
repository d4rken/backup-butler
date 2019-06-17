package eu.darken.bb.common.apps

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PermissionInfo
import eu.darken.bb.common.ApiHelper
import java.util.*

abstract class BasePkgInfo(internal val packageInfo: PackageInfo) : PkgInfo {
    private var labelCache: String? = null

    override val packageName: String
        get() = packageInfo.packageName

    override val applicationInfo: ApplicationInfo?
        get() = packageInfo.applicationInfo

    override val isSystemApp: Boolean
        get() = applicationInfo == null || applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM != 0

    override val versionCode: Long
        @SuppressLint("NewApi")
        get() = if (ApiHelper.hasAndroidP()) packageInfo.longVersionCode else packageInfo.versionCode.toLong()

    override val versionName: String?
        get() = packageInfo.versionName

    override fun getLabel(ipcFunnel: IPCFunnel): String? {
        if (labelCache == null && applicationInfo != null) {
            labelCache = ipcFunnel.submit(IPCFunnel.LabelQuery(appInfo = applicationInfo))
        }
        return labelCache
    }

    override val activities: Collection<ActivityInfo>?
        get() = if (packageInfo.activities == null) null else Arrays.asList(*packageInfo.activities)

    override val receivers: Collection<ActivityInfo>?
        get() = if (packageInfo.receivers == null) null else Arrays.asList(*packageInfo.receivers)

    override val requestedPermissions: Collection<String>?
        get() = if (packageInfo.requestedPermissions == null) null else Arrays.asList(*packageInfo.requestedPermissions)

    override val permissions: Collection<PermissionInfo>?
        get() = if (packageInfo.permissions == null) null else Arrays.asList(*packageInfo.permissions)

    @Throws(Exception::class)
    override fun <T> tryField(fieldName: String): T? = null

    override fun toString(): String = packageName
}