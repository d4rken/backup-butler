package eu.darken.bb.common.file.core

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.IgnoredOnParcel

@JsonClass(generateAdapter = true)
data class Ownership(
        val userId: Int,
        val groupId: Int
) : Parcelable {
    @IgnoredOnParcel @Transient val isValid: Boolean = userId != -1 && groupId != -1

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(userId)
        parcel.writeInt(groupId)
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