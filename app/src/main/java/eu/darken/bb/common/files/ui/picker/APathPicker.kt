package eu.darken.bb.common.files.ui.picker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import eu.darken.bb.R
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.saf.SAFPath
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

object APathPicker {

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
        val persistedPermissions: Set<SAFPath>? = null,
        val payload: Bundle = Bundle()
    ) : Parcelable {

        @IgnoredOnParcel val isCanceled: Boolean = error == null && selection == null
        @IgnoredOnParcel val isSuccess: Boolean = error == null && selection != null
        @IgnoredOnParcel val isFailed: Boolean = error != null && selection == null
    }

    fun fromActivityResult(data: Intent): Result {
        return data.getParcelableExtra(ARG_PICKER_RESULT)!!
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
            Toast.makeText(fragment.requireContext(), R.string.general_error_empty_result_msg, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private const val ARG_PICKER_RESULT = "eu.darken.bb.common.files.ui.picker.APathPicker.Result"
}

