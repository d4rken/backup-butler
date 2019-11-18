package eu.darken.bb.common.root.core.javaroot.fileops

import android.os.Parcel
import android.os.Parcelable
import eu.darken.bb.common.file.core.APath
import eu.darken.bb.common.file.core.APathLookup
import eu.darken.bb.common.readClass
import java.util.*

class RootPathLookup internal constructor(
        override val lookedUp: RootPath,
        override val size: Long,
        val lastModifiedRaw: Long,
        override val fileType: APath.FileType
) : Parcelable, APathLookup {

    override val lastModified: Date = Date(lastModifiedRaw)

    override val name: String = lookedUp.name

    override val path: String = lookedUp.path

    override val pathType: APath.PathType = APath.PathType.LOCAL

    override fun child(vararg segments: String): APath {
        return lookedUp.child(*segments)
    }

    constructor(parcel: Parcel) : this(
            lookedUp = parcel.readClass<RootPath>()!!,
            size = parcel.readLong(),
            lastModifiedRaw = parcel.readLong(),
            fileType = APath.FileType.valueOf(parcel.readString()!!)
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeParcelable(lookedUp, flags)
            writeLong(size)
            writeLong(lastModifiedRaw)
            writeString(fileType.name)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RootPathLookup> {
        override fun createFromParcel(parcel: Parcel): RootPathLookup {
            return RootPathLookup(parcel)
        }

        override fun newArray(size: Int): Array<RootPathLookup?> {
            return arrayOfNulls(size)
        }
    }

}