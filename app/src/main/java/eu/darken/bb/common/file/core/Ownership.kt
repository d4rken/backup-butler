package eu.darken.bb.common.file.core

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Ownership(
        val userId: Long,
        val groupId: Long
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(userId)
        parcel.writeLong(groupId)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Ownership> {
        override fun createFromParcel(parcel: Parcel): Ownership {
            return Ownership(parcel)
        }

        override fun newArray(size: Int): Array<Ownership?> {
            return arrayOfNulls(size)
        }
    }
}