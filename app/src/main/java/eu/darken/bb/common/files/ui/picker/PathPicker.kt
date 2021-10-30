package eu.darken.bb.common.files.ui.picker

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.Keep
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.saf.SAFPath
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

object PathPicker {
    @Keep @Parcelize
    data class Options(
        val startPath: APath? = null,
        val selectionLimit: Int = 1,
        val allowedTypes: Set<APath.PathType> = emptySet(),
        val onlyDirs: Boolean = true,
        val payload: Bundle = Bundle()
    ) : Parcelable {
        @IgnoredOnParcel @Transient val type: APath.PathType? = startPath?.pathType

    }


    @Keep @Parcelize
    data class Result(
        val options: Options,
        val error: Throwable? = null,
        val selection: Set<APath>? = null,
        val persistedPermissions: Set<SAFPath>? = null,
        val payload: Bundle = Bundle()
    ) : Parcelable {

        @IgnoredOnParcel val isCanceled: Boolean = error == null && selection == null
        @IgnoredOnParcel val isSuccess: Boolean = error == null && selection != null
        @IgnoredOnParcel val isFailed: Boolean = error != null
    }
}

