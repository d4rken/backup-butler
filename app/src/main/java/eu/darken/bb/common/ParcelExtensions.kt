package eu.darken.bb.common

import android.os.Parcel
import android.os.Parcelable

fun Parcel.readBoolean(): Boolean {
    return readInt() == 1
}

fun Parcel.writeBoolean(value: Boolean) {
    writeInt(if (value) 1 else 0)
}

inline fun <reified T : Parcelable> Parcel.readClass(): T? {
    return readParcelable(T::class.java.classLoader)
}