package eu.darken.bb.common.previews

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.Resources
import eu.darken.bb.common.pkgs.NormalPkg


data class AppPreviewRequest(val applicationInfo: ApplicationInfo?, val theme: Resources.Theme? = null) {
    constructor(pkg: NormalPkg, context: Context) : this(pkg, context.theme)

    constructor(pkg: NormalPkg, theme: Resources.Theme? = null) : this(pkg.applicationInfo, theme)
}

