package eu.darken.bb.common.pkgs.pkgops.root

import android.content.Context
import eu.darken.bb.App
import eu.darken.bb.common.pkgs.pkgops.LibcoreTool
import eu.darken.bb.common.pkgs.pkgops.installer.RemoteInstallRequest
import eu.darken.bb.common.pkgs.pkgops.installer.routine.DefaultRoutine
import timber.log.Timber

class PkgOpsHost(
        val context: Context
) : PkgOpsConnection.Stub() {

    private val libcoreTool = LibcoreTool()

    override fun install(request: RemoteInstallRequest): Int = try {
        DefaultRoutine(context).install(request)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "install(request=${request.packageName}) failed.")
        throw wrapPropagating(e)
    }

    override fun getUserNameForUID(uid: Int): String? = try {
        libcoreTool.getNameForGid(uid)
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

    private fun wrapPropagating(e: Exception): Exception {
        return if (e is UnsupportedOperationException) e
        else UnsupportedOperationException(e)
    }

    companion object {
        val TAG = App.logTag("Root", "Java", "PkgOps", "Host")
    }


}