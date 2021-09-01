package eu.darken.bb.common.previews.model

import android.content.pm.ApplicationInfo
import android.content.res.Resources

data class AppIconData(
    val applicationInfo: ApplicationInfo,
    val theme: Resources.Theme? = null
)