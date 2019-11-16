package eu.darken.bb.common.pkgs

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import eu.darken.bb.App
import eu.darken.bb.common.ApiHelper
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@PerApp
class PkgRepo @Inject constructor(
        @AppContext private val context: Context,
        private val ipcFunnel: IPCFunnel
) {
    private val requestCache = HashMap<PkgRequest, CachedRequest>()
    private val cacheLock = Any()

    internal class CachedRequest(data: Map<String, Pkg>) {
        private val timestamp: Long = System.currentTimeMillis()
        val data: Map<String, Pkg> = Collections.unmodifiableMap(data)

        fun isStale(newRequest: PkgRequest): Boolean {
            return newRequest.acceptableAge.value != -1L && System.currentTimeMillis() - timestamp > newRequest.acceptableAge.value
        }
    }

    fun getMap(request: PkgRequest = PkgRequest.REFRESH): Map<String, Pkg> {
        var cachedRequest: CachedRequest? = requestCache[request]
        if (cachedRequest == null || cachedRequest.isStale(request)) {
            Timber.tag(TAG).i("Generating new app data for %s", request)
            synchronized(cacheLock) {
                cachedRequest = requestCache[request]
                if (cachedRequest == null || cachedRequest!!.isStale(request)) {
                    val appMap = HashMap<String, Pkg>()

                    var appList: List<PackageInfo>? = ipcFunnel.submit(IPCFunnel.PkgsQuery(request.flags))
                    if (appList == null || appList.isEmpty()) throw IPCBufferException("List of installed apps was empty!")
                    for (pkg in appList) appMap[pkg.packageName] = AppPkg(pkg)

                    @SuppressLint("InlinedApi")
                    val uninstalledFlag = if (ApiHelper.hasAndroidN()) PackageManager.MATCH_UNINSTALLED_PACKAGES else PackageManager.GET_UNINSTALLED_PACKAGES
                    appList = ipcFunnel.submit(IPCFunnel.PkgsQuery(request.flags or uninstalledFlag))
                    if (appList == null) throw IPCBufferException("List of installed apps was empty!")
                    for (pkg in appList) {
                        // https://developer.android.com/reference/android/content/pm/PackageManager.html#MATCH_UNINSTALLED_PACKAGES
                        // Note: this flag may cause less information about currently installed applications to be returned.
                        if (!appMap.containsKey(pkg.packageName)) appMap[pkg.packageName] = AppPkg(pkg)
                    }

                    cachedRequest = CachedRequest(appMap)
                    requestCache[request] = cachedRequest!!
                }
            }
        }
        return cachedRequest!!.data
    }

    companion object {
        private val TAG = App.logTag("PkgRepo")
    }

}
