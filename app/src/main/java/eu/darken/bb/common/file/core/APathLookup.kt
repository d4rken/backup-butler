package eu.darken.bb.common.file.core

import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.common.toOctal
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

@Keep
interface APathLookup<T> : APath {
    val lookedUp: T
    val fileType: APath.FileType
    val size: Long
    val modifiedAt: Date
    val createdAt: Date
    val userId: Long
    val groupId: Long
    val permissions: Permissions
    val target: APath?

    @IgnoredOnParcel val isDirectory: Boolean
        get() = fileType == APath.FileType.DIRECTORY


    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Permissions(
            val mode: Int
    ) : Parcelable {
        @IgnoredOnParcel @Transient val octal: String = mode.toOctal()
    }

}