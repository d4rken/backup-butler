package eu.darken.bb.common.pkgs.pkgops.installer.routine

import android.content.Context
import android.content.pm.PackageInstaller
import android.os.IBinder
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.local.root.DetailedInputSource
import eu.darken.bb.common.files.core.local.root.inputStream
import eu.darken.bb.common.pkgs.pkgops.installer.InstallRoutine
import eu.darken.bb.common.pkgs.pkgops.installer.RemoteInstallRequest
import timber.log.Timber

class DefaultRoutine(private val context: Context, val rootMode: Boolean = false) : InstallRoutine {
    private val installer = context.packageManager.packageInstaller

    /**
     * May be called with ROOT, WHOOP WHOOP, SYSTEM UID 0, or NOT?
     */
    override fun install(request: RemoteInstallRequest): Int {
        Timber.tag(TAG).d("Installing (rootMode=%b): %s", rootMode, request.packageName)
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        params.setAppPackageName(request.packageName)
        val sessionId = installer.createSession(params)
        val session = installer.openSession(sessionId)


        request.apkInputs.map { DetailedInputSource.Stub.asInterface(it as IBinder?) }.forEach { source ->
            val label = source.path().name
            Timber.tag(TAG).v("Writing %s (%d)", source.path().name, source.length())
            session.openWrite(label, 0, -1).use { output ->
                source.input().inputStream().use { it.copyTo(output) }
                session.fsync(output)
            }
            Timber.tag(TAG).v("Finished writing %s", label)
        }

        val pi = createInstallCallback(context, request.packageName)
        Timber.tag(TAG).d("commit(callback=%s)", pi)
        session.commit(pi.intentSender)

//        Cmd.builder("pm install-commit $sessionId").submit(RxCmdShell.builder().root(true).build()).blockingGet()
        return sessionId
    }

    companion object {
        val TAG = logTag("Installer", "DefaultRoutine")
    }
}