package eu.darken.bb.common.pkgs

import eu.darken.bb.common.files.core.APath

data class PkgPathInfo(
        val packageName: String,
        val publicPrimary: APath,
        val publicSecondary: Collection<APath>
)