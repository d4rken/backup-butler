package eu.darken.bb.common

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.Reusable
import eu.darken.bb.App
import eu.darken.bb.common.dagger.AppContext
import timber.log.Timber
import javax.inject.Inject

@Reusable
class RuntimePermissionTool @Inject constructor(
        @AppContext private val context: Context
) {

    fun hasPermission(vararg permssions: String): Boolean {
        val notGranted = permssions.map { Pair(it, ContextCompat.checkSelfPermission(context, it)) }
                .filter { it.second != PackageManager.PERMISSION_GRANTED }
        Timber.tag(TAG).d("Not granted: %s", notGranted)
        return notGranted.isEmpty()
    }

    companion object {
        private val TAG = App.logTag("RuntimePermissionTool")
    }
}