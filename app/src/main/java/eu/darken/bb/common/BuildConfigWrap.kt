package eu.darken.bb.common

import eu.darken.bb.BuildConfig

object BuildConfigWrap {
    const val gitSha: String = BuildConfig.GITSHA
    const val buildTime: String = BuildConfig.BUILDTIME
    const val isBetaBuild = BuildConfig.BETA
    val isDebugBuild = BuildConfig.DEBUG

    val isVerbosebuild: Boolean = isBetaBuild || isDebugBuild
}