package eu.darken.bb.common.pkgs

import android.content.pm.PackageInfo

class AppPkg(packageInfo: PackageInfo) : BasePkgInfo(packageInfo) {

    override val installLocation: Int = packageInfo.installLocation

    override val firstInstallTime: Long = packageInfo.firstInstallTime

    override val lastUpdateTime: Long = packageInfo.lastUpdateTime

    override val sourceDir: String?
        get() {
            if (applicationInfo == null) return null
            return if (applicationInfo!!.sourceDir.isEmpty()) null else applicationInfo!!.sourceDir
        }

    override val packageType: Pkg.Type = Pkg.Type.NORMAL

    @Throws(Exception::class)
    override fun <T> tryField(fieldName: String): T? {
        val field = PackageInfo::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return field.get(packageInfo) as? T
    }
}
