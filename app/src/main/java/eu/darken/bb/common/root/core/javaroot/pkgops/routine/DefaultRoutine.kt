package eu.darken.bb.common.root.core.javaroot.pkgops.routine

import android.content.Context
import android.content.pm.PackageInstaller
import eu.darken.bb.App
import eu.darken.bb.common.root.core.javaroot.fileops.DetailedInputSource
import eu.darken.bb.common.root.core.javaroot.fileops.inputStream
import eu.darken.bb.common.root.core.javaroot.pkgops.InstallRoutine
import eu.darken.bb.common.root.core.javaroot.pkgops.RemoteInstallRequest
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

        @Suppress("UNCHECKED_CAST")
        (request.apkInputs as List<DetailedInputSource>).forEach { source ->
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
        val TAG = App.logTag("Installer", "DefaultRoutine")
    }
}