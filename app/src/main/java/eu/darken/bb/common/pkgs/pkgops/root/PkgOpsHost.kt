package eu.darken.bb.common.pkgs.pkgops.root

import android.app.ActivityManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.pkgs.pkgops.LibcoreTool
import eu.darken.bb.common.pkgs.pkgops.installer.RemoteInstallRequest
import eu.darken.bb.common.pkgs.pkgops.installer.routine.DefaultInstallRoutine
import eu.darken.bb.common.shell.RootProcessShell
import eu.darken.bb.common.shell.SharedShell
import timber.log.Timber
import java.lang.reflect.Method
import javax.inject.Inject


class PkgOpsHost @Inject constructor(
    @ApplicationContext private val context: Context,
    @RootProcessShell private val sharedShell: SharedShell,
    private val libcoreTool: LibcoreTool,
    private val installRoutineFactory: DefaultInstallRoutine.Factory
) : PkgOpsConnection.Stub() {
    override fun install(request: RemoteInstallRequest): Int = try {
        installRoutineFactory.create(rootMode = true).install(request)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "install(request=${request.packageName}) failed.")
        throw wrapPropagating(e)
    }

    override fun getUserNameForUID(uid: Int): String? = try {
        libcoreTool.getNameForUid(uid)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "getUserNameForUID(uid=$uid) failed.")
        throw wrapPropagating(e)
    }

    override fun getGroupNameforGID(gid: Int): String? = try {
        libcoreTool.getNameForGid(gid)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "getGroupNameforGID(gid=$gid) failed.")
        throw wrapPropagating(e)
    }

    override fun forceStop(packageName: String): Boolean = try {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val forceStopPackage: Method = am.javaClass.getDeclaredMethod("forceStopPackage", String::class.java)
        forceStopPackage.isAccessible = true
        forceStopPackage.invoke(am, packageName)
        true
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "forceStop(packageName=$packageName) failed.")
        throw wrapPropagating(e)
    }

    private fun wrapPropagating(e: Exception): Exception {
        return if (e is UnsupportedOperationException) e
        else UnsupportedOperationException(e)
    }

    companion object {
        val TAG = logTag("Root", "Java", "PkgOps", "Host")
    }


}