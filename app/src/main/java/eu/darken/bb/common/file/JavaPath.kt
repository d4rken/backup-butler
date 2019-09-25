package eu.darken.bb.common.file

import java.io.File

data class JavaPath(
        override val type: APath.Type,
        val file: File
) : APath {

    override val pathType: APath.SFileType = APath.SFileType.JAVA

    override val path: String
        get() = file.path

    override val name: String
        get() = file.name

    override fun toString(): String = "JavaFile(file=$file)"

    companion object {
        fun build(type: APath.Type? = null, base: File, vararg crumbs: String): JavaPath {
            return build(type, base.path, *crumbs)
        }

        fun build(type: APath.Type? = null, vararg crumbs: String): JavaPath {
            var compacter = File(crumbs[0])
            for (i in 1 until crumbs.size) {
                compacter = File(compacter, crumbs[i])
            }
            return build(type, compacter)
        }

        fun build(type: APath.Type? = null, file: File): JavaPath {
            var fileType = type
            if (fileType == null) {
                fileType = when {
                    file.isDirectory -> APath.Type.DIRECTORY
                    file.isFile -> APath.Type.FILE
                    else -> throw NotImplementedError("Unknown type: $file")
                }
            }
            return JavaPath(fileType, file.canonicalFile)
        }
    }
}