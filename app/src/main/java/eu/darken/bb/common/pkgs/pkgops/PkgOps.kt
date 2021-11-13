package eu.darken.bb.common.pkgs.pkgops

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Process
import android.os.TransactionTooLargeException
import eu.darken.bb.common.HasSharedResource
import eu.darken.bb.common.SharedResource
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.DeviceEnvironment
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.funnel.IPCFunnel
import eu.darken.bb.common.pkgs.AppPkg
import eu.darken.bb.common.pkgs.NormalPkg
import eu.darken.bb.common.pkgs.Pkg
import eu.darken.bb.common.pkgs.PkgPathInfo
import eu.darken.bb.common.pkgs.pkgops.root.PkgOpsClient
import eu.darken.bb.common.root.javaroot.JavaRootClient
import eu.darken.bb.common.user.UserHandleBB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PkgOps @Inject constructor(
    private val javaRootClient: JavaRootClient,
    private val ipcFunnel: IPCFunnel,
    private val deviceEnvironment: DeviceEnvironment,
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
) : HasSharedResource<Any> {

    override val sharedResource = SharedResource.createKeepAlive(
        TAG,
        appScope + dispatcherProvider.IO
    )

    private suspend fun <T> rootOps(action: (PkgOpsClient) -> T): T {
        sharedResource.keepAliveWith(javaRootClient)
        return javaRootClient.runModuleAction(PkgOpsClient::class.java) {
            return@runModuleAction action(it)
        }
    }

    suspend fun getUserNameForUID(uid: Int): String? = rootOps { client ->
        client.getUserNameForUID(uid)
    }

    suspend fun getGroupNameforGID(gid: Int): String? = rootOps { client ->
        client.getGroupNameforGID(gid)
    }

    fun getUIDForUserName(userName: String): Int? = when (val gid = Process.getUidForName(userName)) {
        -1 -> null
        else -> gid
    }

    fun getGIDForGroupName(groupName: String): Int? = when (val gid = Process.getGidForName(groupName)) {
        -1 -> null
        else -> gid
    }

    suspend fun forceStop(packageName: String): Boolean = rootOps {
        it.forceStop(packageName)
    }

    fun queryPkg(pkgName: String, flags: Int = 0): Pkg? = ipcFunnel.queryPM { pm ->
        var foundPkg: Pkg? = null
        try {
            foundPkg = AppPkg(pm.getPackageInfo(pkgName, flags))
        } catch (e: PackageManager.NameNotFoundException) {
            listPkgs(flags).first { it.packageName == pkgName }
        }
        foundPkg
    }

    fun listPkgs(flags: Int = 0): Collection<Pkg> = ipcFunnel.queryPM { pm ->
        try {
            pm.getInstalledPackages(flags)
        } catch (e: Exception) {
            if (e.cause is TransactionTooLargeException) {
                throw RuntimeException("${IPCFunnel.TAG}:internalGetInstalledPackages($flags):TransactionTooLargeException")
            } else {
                throw RuntimeException(e)
            }
        }.map { AppPkg(it) }.toList()
    }

    fun queryAppInfos(pkg: String, flags: Int = 0): ApplicationInfo? = ipcFunnel.queryPM { pm ->
        try {
            pm.getApplicationInfo(pkg, PackageManager.GET_UNINSTALLED_PACKAGES)
        } catch (e: Throwable) {
            Timber.tag(TAG).d(e)
            null
        }
    }

    fun getLabel(packageName: String): String? = ipcFunnel.queryPM { pm ->
        try {
            pm.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES).loadLabel(pm).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.tag(TAG).w(e)
            null
        }
    }

    fun getLabel(applicationInfo: ApplicationInfo): String? = ipcFunnel.queryPM { pm ->
        try {
            applicationInfo.loadLabel(pm).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.tag(TAG).w(e)
            null
        }
    }

    fun viewArchive(path: String, flags: Int = 0): NormalPkg? = ipcFunnel.queryPM { pm ->
        pm.getPackageArchiveInfo(path, flags)?.let { AppPkg(it) }
    }

    fun getIcon(pkg: String): Drawable? = ipcFunnel.queryPM { pm ->
        try {
            getIcon(pm.getApplicationInfo(pkg, PackageManager.GET_UNINSTALLED_PACKAGES))
        } catch (e: Throwable) {
            Timber.tag(TAG).d(e)
            null
        }
    }

    fun getIcon(appInfo: ApplicationInfo): Drawable? = ipcFunnel.queryPM { pm ->
        try {
            appInfo.loadIcon(pm)
        } catch (e: Throwable) {
            Timber.tag(TAG).d(e)
            null
        }
    }

    fun getPathInfos(packageName: String, userHandle: UserHandleBB): PkgPathInfo {
        val pubPrimary = LocalPath.build(
            deviceEnvironment.getPublicPrimaryStorage(userHandle).localPath,
            "Android",
            "data",
            packageName
        )
        val pubSecondary = deviceEnvironment.getPublicSecondaryStorage(userHandle)
            .map { LocalPath.build(it.localPath, "Android", "data", packageName) }
        return PkgPathInfo(packageName, pubPrimary, pubSecondary)
    }

    companion object {
        val TAG = logTag("PkgsOps")
    }
}