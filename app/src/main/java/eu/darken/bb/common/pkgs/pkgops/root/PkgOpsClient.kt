package eu.darken.bb.common.pkgs.pkgops.root

import eu.darken.bb.App
import eu.darken.bb.common.files.core.local.root.ClientModule
import eu.darken.bb.common.getRootCause
import eu.darken.bb.common.pkgs.pkgops.installer.RemoteInstallRequest
import timber.log.Timber
import java.io.IOException

class PkgOpsClient(
        private val connection: PkgOpsConnection
) : ClientModule {

    fun install(request: RemoteInstallRequest) = try {
        connection.install(request)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "install(request=${request.packageName}) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun getUserNameForUID(uid: Int): String? = try {
        connection.getUserNameForUID(uid)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "getUserNameForUID(uid=$uid) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun getGroupNameforGID(gid: Int): String? = try {
        connection.getGroupNameforGID(gid)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "getGroupNameforGID(gid=$gid) failed.")
        throw fakeIOException(e.getRootCause())
    }

    private fun fakeIOException(e: Throwable): IOException {
        val gulpExceptionPrefix = "java.io.IOException: "
        val message = when {
            e.message.isNullOrEmpty() -> e.toString()
            e.message?.startsWith(gulpExceptionPrefix) == true -> e.message!!.replace(gulpExceptionPrefix, "")
            else -> ""
        }
        return IOException(message, e.cause)
    }

    companion object {
        val TAG = App.logTag("Root", "Java", "PkgOps", "Client")
    }
}