package eu.darken.bb.common.files.ui.picker

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.Keep
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.saf.SAFPath
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Keep @Parcelize
data class PathPickerResult(
    val options: PathPickerOptions,
    val error: Throwable? = null,
    val selection: Set<APath>? = null,
    val persistedPermissions: Set<SAFPath>? = null,
    val payload: Bundle = Bundle()
) : Parcelable {

    @IgnoredOnParcel val isCanceled: Boolean
        get() = error == null && selection == null
    @IgnoredOnParcel val isSuccess: Boolean
        get() = error == null && selection != null
    @IgnoredOnParcel val isFailed: Boolean
        get() = error != null
}