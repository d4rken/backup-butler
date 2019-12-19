package eu.darken.bb.common.pkgs

import eu.darken.bb.common.pkgs.pkgops.PkgOps

interface Pkg {

    val packageName: String

    val versionCode: Long

    val packageType: Type

    fun getLabel(pkgOps: PkgOps): String?

    @Throws(Exception::class)
    fun <T> tryField(fieldName: String): T?

    enum class Type {
        NORMAL, INSTANT
    }
}
