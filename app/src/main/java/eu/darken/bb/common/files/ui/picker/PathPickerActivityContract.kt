package eu.darken.bb.common.files.ui.picker

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import eu.darken.bb.common.debug.logging.log

class PathPickerActivityContract : ActivityResultContract<PathPicker.Options, PathPicker.Result?>() {

    override fun createIntent(context: Context, input: PathPicker.Options): Intent = Intent(
        context,
        PathPickerActivity::class.java
    ).apply {
        log { "createIntent(options=$input)" }
        val bundle = PathPickerActivityArgs(options = input).toBundle()
        putExtras(bundle)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): PathPicker.Result? {
        log { "parseResult(resultCode=$resultCode, intent=$intent)" }
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.getParcelableExtra(ARG_PICKER_RESULT)
    }

    companion object {
        internal const val ARG_PICKER_RESULT = "eu.darken.bb.common.files.ui.picker.PathPicker.Result"
    }
}