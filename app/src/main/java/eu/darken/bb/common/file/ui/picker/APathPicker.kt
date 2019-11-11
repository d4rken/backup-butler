package eu.darken.bb.common.file.ui.picker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import eu.darken.bb.R
import eu.darken.bb.common.file.core.APath
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

object APathPicker {

    @Keep @Parcelize
    data class Options(
            val startPath: APath? = null,
            val selectionLimit: Int = 1,
            val allowedTypes: Set<APath.Type> = emptySet(),
            val onlyDirs: Boolean = true,
            val payload: Bundle = Bundle()
    ) : Parcelable {
        @IgnoredOnParcel @Transient val type: APath.Type? = startPath?.pathType

    }

    fun createIntent(
            context: Context,
            options: Options
    ): Intent {
        val intent = Intent(context, APathPickerActivity::class.java)
        val bundle = APathPickerActivityArgs(options = options).toBundle()
        return intent.putExtras(bundle)
    }

    @Keep @Parcelize
    data class Result(
            val options: Options,
            val error: Throwable? = null,
            val selection: Set<APath>? = null,
            val payload: Bundle = Bundle()
    ) : Parcelable {

        @IgnoredOnParcel val isCanceled: Boolean = error == null && selection == null
        @IgnoredOnParcel val isSuccess: Boolean = error == null && selection != null
        @IgnoredOnParcel val isFailed: Boolean = error != null && selection == null
    }

    fun fromActivityResult(data: Intent): Result {
        return data.getParcelableExtra(ARG_PICKER_RESULT)
    }

    fun toActivityResult(result: Result): Intent {
        return Intent().apply {
            putExtra(ARG_PICKER_RESULT, result)
        }
    }

    fun checkForNonNeutralResult(fragment: Fragment, resultCode: Int, data: Intent?, callback: (Result) -> Unit) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val result = fromActivityResult(data)
            if (!result.isCanceled) callback(result)
        } else if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(fragment.requireContext(), R.string.general_error_empty_result_msg, Toast.LENGTH_SHORT).show()
        }
    }

    private const val ARG_PICKER_RESULT = "eu.darken.bb.common.file.ui.picker.APathPicker.Result"
}

