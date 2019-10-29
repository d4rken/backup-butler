package eu.darken.bb.common.file

import com.squareup.moshi.JsonClass
import eu.darken.bb.common.TypeMissMatchException
import kotlinx.android.parcel.Parcelize
import java.io.File

@JsonClass(generateAdapter = true)
@Parcelize
data class JavaPath(
        val file: File
) : APath {

    override var pathType: APath.Type
        get() = APath.Type.LOCAL
        set(value) {
            TypeMissMatchException.check(value, pathType)
        }

    override val path: String
        get() = file.path

    override val name: String
        get() = file.name

    fun child(vararg segments: String): JavaPath {
        return build(this.file, *segments)
    }

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