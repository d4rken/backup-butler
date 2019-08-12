package eu.darken.bb.common.file

import java.io.File

class JavaFile(
        val file: File
) : SFile {
    override val type: SFile.Type
        get() = when {
            file.isDirectory -> SFile.Type.DIRECTORY
            file.isFile -> SFile.Type.FILE
            else -> throw NotImplementedError("Unknown type: $file")
        }

    override val pathType: SFile.SFileType = SFile.SFileType.JAVA_FILE

    override val path: String
        get() = file.path

    override val name: String
        get() = file.name

    override fun toString(): String {
        return "JavaFile(file=$file)"
    }

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