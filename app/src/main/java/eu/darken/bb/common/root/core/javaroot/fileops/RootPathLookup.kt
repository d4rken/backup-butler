package eu.darken.bb.common.root.core.javaroot.fileops

import android.os.Parcel
import android.os.Parcelable
import eu.darken.bb.common.file.core.APath
import eu.darken.bb.common.file.core.APathLookup
import eu.darken.bb.common.readClass
import java.util.*

class RootPathLookup internal constructor(
        override val lookedUp: RootPath,
        override val fileType: APath.FileType,
        override val size: Long,
        val lastModifiedRaw: Long,
        override val target: RootPath?
) : Parcelable, APathLookup<RootPath> {

    override val lastModified: Date = Date(lastModifiedRaw)

    override val name: String = lookedUp.name

    override val path: String = lookedUp.path

    override val pathType: APath.PathType = APath.PathType.LOCAL

    override fun child(vararg segments: String): APath {
        return lookedUp.child(*segments)
    }

    constructor(parcel: Parcel) : this(
            lookedUp = parcel.readClass<RootPath>()!!,
            fileType = APath.FileType.valueOf(parcel.readString()!!),
            size = parcel.readLong(),
            lastModifiedRaw = parcel.readLong(),
            target = parcel.readClass<RootPath>()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeParcelable(lookedUp, flags)
            writeString(fileType.name)
            writeLong(size)
            writeLong(lastModifiedRaw)
            writeParcelable(target, flags)
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