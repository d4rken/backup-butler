package eu.darken.bb.common.files.core.local

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.common.TypeMissMatchException
import eu.darken.bb.common.files.core.APath
import java.io.File


@Keep
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

    override fun toString(): String = "LocalPath(file=$file)"

    constructor(parcel: Parcel) : this(File(parcel.readString()))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(file.path)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField val CREATOR = object : Parcelable.Creator<LocalPath> {
            override fun createFromParcel(parcel: Parcel): LocalPath {
                return LocalPath(parcel)
            }

            override fun newArray(size: Int): Array<LocalPath?> {
                return arrayOfNulls(size)
            }
        }

        fun build(base: LocalPath, vararg crumbs: String): LocalPath {
            return build(base.path, *crumbs)
        }

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
            return LocalPath(file)
        }
    }


}