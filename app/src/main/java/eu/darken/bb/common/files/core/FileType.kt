package eu.darken.bb.common.files.core

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
enum class FileType : Parcelable {
    DIRECTORY, SYMBOLIC_LINK, FILE
}