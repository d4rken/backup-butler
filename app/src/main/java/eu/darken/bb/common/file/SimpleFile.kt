package eu.darken.bb.common.file

import java.io.File

data class SimpleFile(
        override val path: String
) : SFile {

    override val pathType: SFile.PathType = SFile.PathType.SIMPLE

    override val name: String = path.substringAfterLast(File.separatorChar)

    companion object {
        fun build(vararg crumbs: String): SimpleFile {
            var compacter = File(crumbs[0])
            for (i in 1 until crumbs.size) {
                compacter = File(compacter, crumbs[i])
            }
            return SimpleFile(compacter.path)
        }
    }
}