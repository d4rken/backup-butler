package eu.darken.bb.common.file.core.local

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.common.TypeMissMatchException
import eu.darken.bb.common.file.core.APath
import kotlinx.android.parcel.Parcelize
import java.io.File
import java.io.IOException


@Keep @Parcelize
@JsonClass(generateAdapter = true)
data class LocalPath(
        val file: File
) : APath {

    override var pathType: APath.PathType
        get() = APath.PathType.LOCAL
        set(value) {
            TypeMissMatchException.check(value, pathType)
        }

    override val path: String
        get() = file.path

    override val name: String
        get() = file.name

    override fun child(vararg segments: String): LocalPath {
        return build(this.file, *segments)
    }

    @Throws(IOException::class)
    fun lookup(gateway: LocalGateway): LocalPathLookup = gateway.lookup(this)

    @Throws(IOException::class)
    fun listFiles(gateway: LocalGateway): List<LocalPath> = gateway.listFiles(this)

    @Throws(IOException::class)
    fun canWrite(gateway: LocalGateway) = gateway.canWrite(this)

    @Throws(IOException::class)
    fun canRead(gateway: LocalGateway) = gateway.canRead(this)

    @Throws(IOException::class)
    fun delete(gateway: LocalGateway): Boolean = gateway.delete(this)

    @Throws(IOException::class)
    fun exists(gateway: LocalGateway): Boolean = gateway.exists(this)

    @Throws(IOException::class)
    fun isFile(gateway: LocalGateway): Boolean = gateway.lookup(this).fileType == APath.FileType.FILE

    @Throws(IOException::class)
    fun isDirectory(gateway: LocalGateway): Boolean = gateway.lookup(this).fileType == APath.FileType.DIRECTORY

    override fun toString(): String = "LocalPath(file=$file)"

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