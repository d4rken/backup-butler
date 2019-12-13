package eu.darken.bb.common.root.core.javaroot.pkgops

import eu.darken.bb.App
import eu.darken.bb.common.getRootCause
import eu.darken.bb.common.root.core.javaroot.fileops.ClientModule
import timber.log.Timber
import java.io.IOException

class PkgOpsClient(
        private val pkgOps: PkgOps
) : ClientModule {

    fun install(request: RemoteInstallRequest) = try {
        pkgOps.install(request)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "install(request=${request.packageName}) failed.")
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