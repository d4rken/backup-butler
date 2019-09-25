package eu.darken.bb.common.file

import java.io.File

data class JavaPath(
        val file: File
) : APath {

    override val pathType: APath.SFileType = APath.SFileType.JAVA

    override val path: String
        get() = file.path

    override val name: String
        get() = file.name

    override fun toString(): String = "JavaFile(file=$file)"

    companion object {
        fun build(base: File, vararg crumbs: String): JavaPath {
            return build(base.path, *crumbs)
        }

        fun build(vararg crumbs: String): JavaPath {
            var compacter = File(crumbs[0])
            for (i in 1 until crumbs.size) {
                compacter = File(compacter, crumbs[i])
            }
            return build(compacter)
        }

        fun build(file: File): JavaPath {
            return JavaPath(file.canonicalFile)
        }
    }
}