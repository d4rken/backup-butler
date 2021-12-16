package eu.darken.bb.common.permission

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.hasApiLevel
import javax.inject.Inject

@Reusable
class RuntimePermissionTool @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun hasPermission(vararg permssions: Permission): Boolean = permssions.map { it.check(context) }.all { it }

    fun hasStoragePermission(): Boolean {
        val granted = if (hasApiLevel(Build.VERSION_CODES.R)) {
            @Suppress("NewApi")
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Permission.WRITE_EXTERNAL_STORAGE.id
            ) == PackageManager.PERMISSION_GRANTED
        }
        log(TAG) { "Granted: $granted" }
        return granted
    }

    fun getRequiredStoragePermission(): Permission = when {
        hasApiLevel(Build.VERSION_CODES.R) -> Permission.MANAGE_EXTERNAL_STORAGE
        else -> Permission.WRITE_EXTERNAL_STORAGE
    }

    companion object {
        private val TAG = logTag("RuntimePermissionTool")
    }
}
