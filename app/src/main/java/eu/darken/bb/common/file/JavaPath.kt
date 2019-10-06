package eu.darken.bb.common.file

import com.squareup.moshi.JsonClass
import java.io.File

@JsonClass(generateAdapter = true)
data class JavaPath(
        val file: File
) : APath {

    override var pathType: APath.SFileType
        get() = APath.SFileType.JAVA
        set(value) {}

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