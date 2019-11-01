package eu.darken.bb.common.pkgs

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.TransactionTooLargeException
import eu.darken.bb.App
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.file.APath
import timber.log.Timber
import java.util.concurrent.Semaphore
import javax.inject.Inject

/**
 * Tries to reduce the chance that we hit the IPC buffer limit.
 * Hitting the buffer limit can result in crashes or more grave incomplete results.
 */
@PerApp
class IPCFunnel @Inject constructor(
        @AppContext context: Context
) {
    private val funnelLock = Semaphore(1)
    private val packageManager: PackageManager = context.packageManager

    init {
        Timber.tag(TAG).d("IPCFunnel initialized.")
    }

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

    data class PkgQuery(val pkgName: String, val flags: Int = 0) : PMQuery<Pkg> {

        override fun onPackManAction(pm: PackageManager): Pkg? {
            var packageInfo: PackageInfo? = null
            try {
                packageInfo = pm.getPackageInfo(pkgName, flags)
            } catch (e: PackageManager.NameNotFoundException) {
                val pkgs = PkgsQuery(flags).onPackManAction(pm)
                for (pkg in requireNotNull(pkgs)) {
                    if (pkg.packageName == pkgName) {
                        packageInfo = pkg
                        break
                    }
                }
            }

            return if (packageInfo != null) AppPkg(packageInfo) else null
        }
    }

    data class PkgsQuery(private val flags: Int) : PMQuery<List<PackageInfo>> {

        override fun onPackManAction(pm: PackageManager): List<PackageInfo>? {
            val ret: List<PackageInfo>
            try {
                ret = pm.getInstalledPackages(flags)
            } catch (e: Exception) {
                if (e.cause is TransactionTooLargeException) {
                    throw RuntimeException("$TAG:internalGetInstalledPackages($flags):TransactionTooLargeException")
                } else {
                    throw RuntimeException(e)
                }
            }

            return ret
        }
    }

    class LabelQuery : PMQuery<String> {
        val packageName: String?
        val applicationInfo: ApplicationInfo?

        constructor(packageName: String) {
            this.packageName = packageName
            this.applicationInfo = null
        }

        constructor(applicationInfo: ApplicationInfo) {
            this.applicationInfo = applicationInfo
            this.packageName = null
        }

        override fun onPackManAction(pm: PackageManager): String? {
            return try {
                var appInfo = applicationInfo
                if (appInfo == null) appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES)
                appInfo!!.loadLabel(pm).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                Timber.tag(TAG).w(e)
                null
            }
        }
    }

    data class ArchiveQuery(val path: String, val flags: Int = 0) : PMQuery<Pkg> {
        constructor(file: APath, flags: Int) : this(file.path, flags)

        override fun onPackManAction(pm: PackageManager): Pkg? {
            val info = pm.getPackageArchiveInfo(path, flags)
            return if (info != null) AppPkg(info) else null
        }
    }

    data class InstallerQuery(val packageName: String) : PMQuery<String> {

        override fun onPackManAction(pm: PackageManager): String? {
            return try {
                pm.getInstallerPackageName(packageName)
            } catch (e: Throwable) {
                Timber.tag(TAG).d(e)
                null
            }
        }
    }

    data class IconQuery(val packageName: String?, val applicationInfo: ApplicationInfo?) : PMQuery<Drawable> {

        override fun onPackManAction(pm: PackageManager): Drawable? {
            return try {
                var appInfo = applicationInfo
                if (appInfo == null) appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES)
                appInfo!!.loadIcon(pm)
            } catch (e: Throwable) {
                Timber.tag(TAG).d(e)
                null
            }

        }
    }

    companion object {
        internal val TAG = App.logTag("IPCFunnel")
    }
}
