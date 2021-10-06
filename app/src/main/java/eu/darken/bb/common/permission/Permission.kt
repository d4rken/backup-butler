package eu.darken.bb.common.permission

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import eu.darken.bb.R
import eu.darken.bb.common.CaString
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.toCaString

enum class Permission(
    val id: String,
    val label: CaString,
    val description: CaString,
) {

    WRITE_EXTERNAL_STORAGE(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE.toCaString(),
        R.string.permission_write_external_storage_description.toCaString(),
    ),

    @SuppressLint("InlinedApi")
    MANAGE_EXTERNAL_STORAGE(
        Manifest.permission.MANAGE_EXTERNAL_STORAGE,
        Manifest.permission.MANAGE_EXTERNAL_STORAGE.toCaString(),
        R.string.permission_manage_external_storage_description.toCaString(),
    ) {
        override fun check(context: Context): Boolean = Environment.isExternalStorageManager()

        override fun setup(fragment: Fragment, callback: (Permission, Boolean) -> Unit): Launcher {
            log { "Setup Permission.Launcher for $id via $fragment" }
            val launcher =
                fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    log { "Request for MANAGE_EXTERNAL_STORAGE was result=$result" }
                    callback(this, Environment.isExternalStorageManager())
                }
            return object : Launcher {
                override fun launch() {
                    log { "Launching request for $id" }
                    launcher.launch(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                }
            }
        }
    }
    ;

    open fun check(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, id) == PackageManager.PERMISSION_GRANTED


    interface Launcher {
        fun launch()
    }

    open fun setup(fragment: Fragment, callback: (Permission, Boolean) -> Unit): Launcher {
        log { "Setup Permission.Launcher for $id via $fragment" }
        val launcher = fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            log { "Request for $id was granted=$granted" }
            callback(this, granted)
        }
        return object : Launcher {
            override fun launch() {
                log { "Launching request for $id" }
                launcher.launch(id)
            }
        }
    }

}
