package eu.darken.bb.common.apps

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.TransactionTooLargeException
import eu.darken.bb.App
import eu.darken.bb.AppComponent
import eu.darken.bb.common.dagger.ApplicationContext
import timber.log.Timber
import java.util.concurrent.Semaphore
import javax.inject.Inject

/**
 * Tries to reduce the chance that we hit the IPC buffer limit.
 * Hitting the buffer limit can result in crashes or more grave incomplete results.
 */
@AppComponent.Scope
class IPCFunnel @Inject constructor(
        @ApplicationContext private val packageManager: PackageManager
) {
    companion object {
        internal val TAG = App.logTag("IPCFunnel")
    }

    private val funnelLock = Semaphore(1)

    fun <T> submit(action: PMQuery<T>): T? {
        try {
            funnelLock.acquire()
            return action.onPackManAction(packageManager)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } finally {
            funnelLock.release()
        }
    }

    interface PMQuery<T> {
        fun onPackManAction(pm: PackageManager): T?
    }

    data class PkgQuery @JvmOverloads constructor(val pkgName: String, val flags: Int = 0) : PMQuery<PkgInfo> {

        override fun onPackManAction(pm: PackageManager): PkgInfo? {
            var packageInfo: PackageInfo? = null
            try {
                packageInfo = pm.getPackageInfo(pkgName, flags)
            } catch (e: PackageManager.NameNotFoundException) {
                val pkgs = PkgsQuery(flags).onPackManAction(pm)!!
                for (pkg in pkgs) {
                    if (pkg.packageName == pkgName) {
                        packageInfo = pkg
                        break
                    }
                }
            }
            return if (packageInfo != null) NormalApp(packageInfo) else null
        }
    }

    data class PkgsQuery(private val flags: Int) : PMQuery<List<PackageInfo>> {

        override fun onPackManAction(pm: PackageManager): List<PackageInfo>? {
            try {
                return pm.getInstalledPackages(flags)
            } catch (e: Exception) {
                if (e.cause is TransactionTooLargeException) {
                    throw RuntimeException("$TAG:internalGetInstalledPackages($flags):TransactionTooLargeException")
                } else {
                    throw RuntimeException(e)
                }
            }
        }
    }

    data class LabelQuery constructor(
            private val packageName: String? = null,
            private val appInfo: ApplicationInfo? = null

    ) : PMQuery<String> {

        override fun onPackManAction(pm: PackageManager): String? {
            return try {
                var appInfo = appInfo
                if (appInfo == null) {
                    appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES)
                }
                appInfo!!.loadLabel(pm).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                Timber.tag(TAG).w(e)
                null
            }

        }
    }

    data class ArchiveFileQuery(private val path: String, private val flags: Int) : PMQuery<PkgInfo> {
        override fun onPackManAction(pm: PackageManager): PkgInfo? {
            val info = pm.getPackageArchiveInfo(path, flags)
            return if (info != null) NormalApp(info) else null
        }
    }
}
