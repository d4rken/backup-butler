package eu.darken.bb.common.file.picker.native

import android.content.Intent
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import eu.darken.bb.App
import eu.darken.bb.common.file.APath
import eu.darken.bb.common.file.asSFile
import timber.log.Timber
import javax.inject.Inject

class FilePicker @Inject constructor() {
    fun getResult(requestCode: Int, resultCode: Int, data: Intent?): Collection<APath> {
        Timber.tag(TAG).v("getResult(requestCode=%d, resultCode=%d, data=%s)", requestCode, resultCode, data)
        TODO()
    }

    fun launchPicker(fragment: Fragment, options: Options = Options()) {
        TODO("not implemented")
    }

    fun launchPicker(activity: AppCompatActivity, options: Options = Options()) {
        TODO("not implemented")
    }

    data class Options(
            val pathRoot: APath = Environment.getExternalStorageDirectory().asSFile()
    )

    companion object {
        val TAG = App.logTag("FilePicker")
    }
}