package eu.darken.bb.common.file

import java.io.File

data class JavaFile(val file: File) : SFile {

    override val pathType: SFile.PathType = SFile.PathType.JAVA_FILE

    override val path: String
        get() = file.path

    override val name: String
        get() = file.name

    companion object {
        fun build(base: File, vararg crumbs: String): JavaFile {
            return build(base.path, *crumbs)
        }

        fun build(vararg crumbs: String): JavaFile {
            var compacter = File(crumbs[0])
            for (i in 1 until crumbs.size) {
                compacter = File(compacter, crumbs[i])
            }
            return JavaFile(compacter.canonicalFile)
        }
    }
}