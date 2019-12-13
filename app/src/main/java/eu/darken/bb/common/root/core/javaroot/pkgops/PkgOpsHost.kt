package eu.darken.bb.common.root.core.javaroot.pkgops

import android.content.Context
import eu.darken.bb.App
import eu.darken.bb.common.root.core.javaroot.pkgops.routine.DefaultRoutine
import timber.log.Timber

class PkgOpsHost(val context: Context) : PkgOps.Stub() {

    override fun install(request: RemoteInstallRequest): Int = try {
        DefaultRoutine(context).install(request)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "install(request=${request.packageName}) failed.")
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