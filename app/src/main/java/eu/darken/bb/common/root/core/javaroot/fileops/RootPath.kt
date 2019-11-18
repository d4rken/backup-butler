package eu.darken.bb.common.root.core.javaroot.fileops

import android.os.Parcel
import android.os.Parcelable
import eu.darken.bb.common.file.core.APath
import java.io.File

class RootPath internal constructor(
        override val path: String
) : Parcelable, APath {

    private val file: File = File(path)

    override val name: String = file.name

    override val pathType: APath.PathType = APath.PathType.LOCAL

    override fun child(vararg segments: String): APath {
        return build(file, *segments)
    }

    constructor(parcel: Parcel) : this(parcel.readString()!!)

    override fun writeToParcel(dest: Parcel, flags: Int) = dest.writeString(path)

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<RootPath> {

        override fun createFromParcel(parcel: Parcel): RootPath {
            return RootPath(parcel)
        }

        override fun newArray(size: Int): Array<RootPath?> {
            return arrayOfNulls(size)
        }

        fun build(base: File, vararg crumbs: String): RootPath {
            return build(base.path, *crumbs)
        }

        fun build(vararg crumbs: String): RootPath {
            var compacter = File(crumbs[0])
            for (i in 1 until crumbs.size) {
                compacter = File(compacter, crumbs[i])
            }
            return build(compacter)
        }

        fun build(file: File): RootPath {
            return RootPath(file.path)
        }
    }

}