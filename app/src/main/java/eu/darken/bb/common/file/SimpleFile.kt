package eu.darken.bb.common.file

import java.io.File

data class SimpleFile(
        override val type: SFile.Type,
        override val path: String
) : SFile {

    override val pathType: SFile.SFileType = SFile.SFileType.SIMPLE

    override val name: String = path.substringAfterLast(File.separatorChar)

    companion object {
        fun build(type: SFile.Type, base: File, vararg crumbs: String): SimpleFile =
                build(type, base.canonicalPath, *crumbs)

        fun build(type: SFile.Type, vararg crumbs: String): SimpleFile {
            var compacter = File(crumbs[0])
            for (i in 1 until crumbs.size) {
                compacter = File(compacter, crumbs[i])
            }
            return SimpleFile(type, compacter.path)
        }
    }
}