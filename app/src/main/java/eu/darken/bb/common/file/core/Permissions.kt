package eu.darken.bb.common.file.core

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.JsonClass
import eu.darken.bb.common.toOctal
import kotlinx.android.parcel.IgnoredOnParcel

@JsonClass(generateAdapter = true)
data class Permissions(
        val mode: Int
) : Parcelable {
    @IgnoredOnParcel @Transient val octal: String = mode.toOctal()

    constructor(parcel: Parcel) : this(parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(mode)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Permissions> {
        override fun createFromParcel(parcel: Parcel): Permissions {
            return Permissions(parcel)
        }

        override fun newArray(size: Int): Array<Permissions?> {
            return arrayOfNulls(size)
        }
    }
}