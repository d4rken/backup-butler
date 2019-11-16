package eu.darken.bb.common.file.core.local

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.common.TypeMissMatchException
import eu.darken.bb.common.file.core.APath
import kotlinx.android.parcel.Parcelize
import java.io.File


@Keep @Parcelize
@JsonClass(generateAdapter = true)
data class LocalPath(
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

    fun child(vararg segments: String): LocalPath {
        return build(this.file, *segments)
    }

    fun isDirectory(gateway: LocalGateway): Boolean = gateway.isDirectory(this)

    fun cached(gateway: LocalGateway): LocalPathCached = gateway.buildCachedVersion(this)

    override fun toString(): String = "JavaFile(file=$file)"

    companion object {
        fun build(base: File, vararg crumbs: String): LocalPath {
            return build(base.path, *crumbs)
        }

        fun build(vararg crumbs: String): LocalPath {
            var compacter = File(crumbs[0])
            for (i in 1 until crumbs.size) {
                compacter = File(compacter, crumbs[i])
            }
            return build(compacter)
        }

        fun build(file: File): LocalPath {
            return LocalPath(file.canonicalFile)
        }
    }
}