package eu.darken.bb.common.pkgs.pkgops.installer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.pkgs.pkgops.installer.InstallerReceiver.InstallEvent.Code
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class InstallerReceiver : BroadcastReceiver() {

    @Inject lateinit var installer: APKInstaller

    override fun onReceive(context: Context, intent: Intent) {
        Timber.tag(TAG).d("onReceive(context=%s, intent=%s", context, intent)

        val code = when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
            PackageInstaller.STATUS_SUCCESS -> Code.SUCCESS
            PackageInstaller.STATUS_PENDING_USER_ACTION -> Code.USER_ACTION
            else -> Code.ERROR
        }

        val sessionId = intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1)

        val event = InstallEvent(
            code = code,
            statusMessage = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE),
            packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME),
            sessionId = if (sessionId == -1) null else sessionId,
            userAction = intent.getParcelableExtra(Intent.EXTRA_INTENT)
        )

        Timber.tag(TAG).d("Processing event %s", event)
        installer.handleEvent(event)
    }

    data class InstallEvent(
        val code: Code,
        val statusMessage: String?,
        val packageName: String?,
        val sessionId: Int?,
        val userAction: Intent?
    ) {
        enum class Code {
            SUCCESS, ERROR, USER_ACTION
        }
    }

    companion object {
        val TAG = logTag("Installer", "Receiver")
    }
}