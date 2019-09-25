package eu.darken.bb.common.file

import java.io.File

data class SimplePath(
        override val type: APath.Type,
        override val path: String
) : APath {

    override val pathType: APath.SFileType = APath.SFileType.SIMPLE

    override val name: String = path.substringAfterLast(File.separatorChar)

    companion object {
        fun build(type: APath.Type, base: File, vararg crumbs: String): SimplePath =
                build(type, base.canonicalPath, *crumbs)

        fun build(type: APath.Type, vararg crumbs: String): SimplePath {
            var compacter = File(crumbs[0])
            for (i in 1 until crumbs.size) {
                compacter = File(compacter, crumbs[i])
            }
            return SimplePath(type, compacter.path)
        }
    }
}