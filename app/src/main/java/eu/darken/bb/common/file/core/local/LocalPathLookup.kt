package eu.darken.bb.common.file.core.local

import android.os.Parcel
import android.os.Parcelable
import eu.darken.bb.common.file.core.APath
import eu.darken.bb.common.file.core.APathLookup
import kotlinx.android.parcel.IgnoredOnParcel
import java.util.*

data class LocalPathLookup(
        override val lookedUp: LocalPath,
        override val fileType: APath.FileType,
        override val size: Long,
        override val modifiedAt: Date,
        override val createdAt: Date,
        override val userId: Long,
        override val groupId: Long,
        override val permissions: APathLookup.Permissions,
        override val target: LocalPath?
) : APathLookup<LocalPath> {
    override fun child(vararg segments: String): APath {
        return lookedUp.child(*segments)
    }

    @IgnoredOnParcel override val path: String = lookedUp.path
    @IgnoredOnParcel override val name: String = lookedUp.name
    @IgnoredOnParcel override val pathType: APath.PathType = lookedUp.pathType

    constructor(parcel: Parcel) : this(
            parcel.readParcelable(LocalPath::class.java.classLoader)!!,
            parcel.readParcelable(APath.FileType::class.java.classLoader)!!,
            parcel.readLong(),
            Date(parcel.readLong()),
            Date(parcel.readLong()),
            parcel.readLong(),
            parcel.readLong(),
            parcel.readParcelable(APathLookup.Permissions::class.java.classLoader)!!,
            parcel.readParcelable(LocalPath::class.java.classLoader))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(lookedUp, flags)
        parcel.writeParcelable(fileType, flags)
        parcel.writeLong(size)
        parcel.writeLong(modifiedAt.time)
        parcel.writeLong(createdAt.time)
        parcel.writeLong(userId)
        parcel.writeLong(groupId)
        parcel.writeParcelable(permissions, flags)
        parcel.writeParcelable(target, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<LocalPathLookup> {
        override fun createFromParcel(parcel: Parcel): LocalPathLookup {
            return LocalPathLookup(parcel)
        }

        override fun newArray(size: Int): Array<LocalPathLookup?> {
            return arrayOfNulls(size)
        }
    }

}