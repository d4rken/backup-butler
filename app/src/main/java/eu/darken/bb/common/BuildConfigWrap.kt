package eu.darken.bb.common

import eu.darken.bb.BuildConfig

object BuildConfigWrap {
    val gitSha: String = BuildConfig.GITSHA
    val buildTime: String = BuildConfig.BUILDTIME
    val isBetaBuild = BuildConfig.BETA
    val isDebugBuild = BuildConfig.DEBUG
}