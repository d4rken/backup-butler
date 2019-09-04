package eu.darken.bb.common.file

import java.io.File

data class JavaFile(
        override val type: SFile.Type,
        val file: File
) : SFile {

    override val pathType: SFile.SFileType = SFile.SFileType.JAVA

    override val path: String
        get() = file.path

    override val name: String
        get() = file.name

    override val parent: SFile
        get() = file.parentFile.asSFile()

    override fun toString(): String = "JavaFile(file=$file)"

    companion object {
        fun build(type: SFile.Type? = null, base: File, vararg crumbs: String): JavaFile {
            return build(type, base.path, *crumbs)
        }

        fun build(type: SFile.Type? = null, vararg crumbs: String): JavaFile {
            var compacter = File(crumbs[0])
            for (i in 1 until crumbs.size) {
                compacter = File(compacter, crumbs[i])
            }
            return build(type, compacter)
        }

        fun build(type: SFile.Type? = null, file: File): JavaFile {
            var fileType = type
            if (fileType == null) {
                fileType = when {
                    file.isDirectory -> SFile.Type.DIRECTORY
                    file.isFile -> SFile.Type.FILE
                    else -> throw NotImplementedError("Unknown type: $file")
                }
            }
            return JavaFile(fileType, file.canonicalFile)
        }
    }
}