package eu.darken.bb.common.files.ui.picker

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.Keep
import eu.darken.bb.common.files.core.APath
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Keep @Parcelize
data class PathPickerOptions(
    val startPath: APath? = null,
    val selectionLimit: Int = 1,
    val allowedTypes: Set<APath.PathType> = emptySet(),
    val onlyDirs: Boolean = true,
    val allowCreateDir: Boolean = true,
    val payload: Bundle = Bundle()
) : Parcelable {
    @IgnoredOnParcel @Transient val type: APath.PathType? = startPath?.pathType

}