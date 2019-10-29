package eu.darken.bb.common.file.picker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import eu.darken.bb.common.file.APath
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

object APathPicker {

    @Parcelize
    data class Options(
            val startPath: APath? = null,
            val selectionLimit: Int = 1,
            val allowTypeChange: Boolean = true,
            val payload: Bundle = Bundle()
    ) : Parcelable {
        @IgnoredOnParcel @Transient val type: APath.Type? = startPath?.pathType

    }

    fun createIntent(
            context: Context,
            options: Options = Options()
    ): Intent {
        val intent = Intent(context, APathPickerActivity::class.java)

        return intoIntent(intent, options)
    }

    @Parcelize
    data class Result(
            val options: Options,
            val error: Throwable? = null,
            val path: APath? = null,
            val payload: Bundle = Bundle()
    ) : Parcelable

    fun fromActivityResult(data: Intent): Result {
        return data.getParcelableExtra(ARG_PICKER_RESULT)
    }

    fun fromIntent(intent: Intent): Options {
        return intent.getParcelableExtra(ARG_PICKER_OPTIONS)
    }

    fun intoIntent(intent: Intent, options: Options): Intent {
        return intent.putExtra(ARG_PICKER_OPTIONS, options)
    }

    fun toActivityResult(result: Result): Intent {
        return Intent().apply {
            putExtra(ARG_PICKER_RESULT, result)
        }
    }

    private const val ARG_PICKER_OPTIONS = "eu.darken.bb.common.file.picker.APathPicker.Options"
    private const val ARG_PICKER_RESULT = "eu.darken.bb.common.file.picker.APathPicker.Result"
}