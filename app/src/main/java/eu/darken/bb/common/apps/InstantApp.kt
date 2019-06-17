package eu.darken.bb.common.apps

import android.content.pm.PackageInfo

class InstantApp(packageInfo: PackageInfo, private val sourcePath: String) : BasePkgInfo(packageInfo) {

    override var lastUpdateTime: Long = 0

    override var firstInstallTime: Long = 0

    override val installLocation: Int
        get() = 0

    override val isInstantApp: Boolean
        get() = true

    override val sourceDir: String?
        get() = sourcePath
}
