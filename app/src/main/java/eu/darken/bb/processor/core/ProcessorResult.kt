package eu.darken.bb.processor.core

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProcessorResult(
    val request: ProcessorRequest,
    val error: Throwable? = null
) : Parcelable {

    @IgnoredOnParcel
    val isSuccess: Boolean
        get() = error != null
}