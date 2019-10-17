package eu.darken.bb.common.file

import eu.darken.bb.common.TypeMissMatchException
import java.io.File

data class SimplePath(
        override val path: String
) : APath {

    override var pathType: APath.SFileType
        get() = APath.SFileType.SIMPLE
        set(value) {
            TypeMissMatchException.check(value, pathType)
        }

    override val name: String = path.substringAfterLast(File.separatorChar)

    companion object {
        fun build(base: File, vararg crumbs: String): SimplePath =
                build(base.canonicalPath, *crumbs)

        fun build(vararg crumbs: String): SimplePath {
            var compacter = File(crumbs[0])
            for (i in 1 until crumbs.size) {
                compacter = File(compacter, crumbs[i])
            }
            return SimplePath(compacter.path)
        }
    }
}