package eu.darken.bb.common.apps

import android.content.pm.PackageInfo

class NormalApp(packageInfo: PackageInfo) : BasePkgInfo(packageInfo) {

    override val installLocation: Int
        get() = packageInfo.installLocation

    override val firstInstallTime: Long
        get() = packageInfo.firstInstallTime
    override val lastUpdateTime: Long
        get() = packageInfo.lastUpdateTime

    override val isInstantApp: Boolean
        get() = false

    override val sourceDir: String?
        get() {
            if (applicationInfo == null) return null
            if (applicationInfo!!.sourceDir.isNullOrEmpty()) return null
            return applicationInfo!!.sourceDir
        }

    @Suppress("UNCHECKED_CAST")
    @Throws(Exception::class)
    override fun <T> tryField(fieldName: String): T? {
        val field = PackageInfo::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(packageInfo) as T
    }
}
