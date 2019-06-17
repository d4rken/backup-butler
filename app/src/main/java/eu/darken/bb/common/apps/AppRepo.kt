package eu.darken.bb.common.apps

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import eu.darken.bb.App
import eu.darken.bb.AppComponent
import eu.darken.bb.common.ApiHelper
import eu.darken.bb.common.dagger.ApplicationContext
import eu.darken.bb.common.root.RootManager
import eu.darken.rxshell.cmd.Cmd
import eu.darken.rxshell.cmd.RxCmdShell
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.InputSource
import timber.log.Timber
import java.io.File
import java.io.FileReader
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import java.util.regex.Pattern
import javax.inject.Inject
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.concurrent.withLock

@AppComponent.Scope
class AppRepo @Inject constructor(
        @ApplicationContext private val context: Context,
        private val rootManager: RootManager,
        private val ipcFunnel: IPCFunnel
) {

    companion object {
        private val TAG = App.logTag("AppRepo")
    }

    private val requestCache = HashMap<AppsRequest, CachedRequest>()
    private val lock = ReentrantLock()
    private val pattern = Pattern.compile("^(?:package:)(.+?)(?:=)([\\w._]+)$")

    internal class CachedRequest(data: Map<String, PkgInfo>) {
        private val timestamp: Long = System.currentTimeMillis()
        val data: Map<String, PkgInfo> = Collections.unmodifiableMap(data)

        fun isStale(newRequest: AppsRequest): Boolean {
            return newRequest.acceptableAge.age != -1L && System.currentTimeMillis() - timestamp > newRequest.acceptableAge.age
        }
    }

    fun getAppMap(request: AppsRequest): Map<String, PkgInfo> {
        var cachedRequest: CachedRequest? = requestCache[request]
        if (cachedRequest == null || cachedRequest.isStale(request)) {
            Timber.tag(TAG).i("Generating new app data for %s", request)
            lock.withLock {
                cachedRequest = requestCache[request]
                if (cachedRequest == null || cachedRequest!!.isStale(request)) {
                    val appMap = HashMap<String, PkgInfo>()

                    var appList = ipcFunnel.submit(IPCFunnel.PkgsQuery(request.flags))
                    if (appList == null || appList.isEmpty()) throw IPCBufferException("List of installed apps was empty!")
                    for (pkg in appList) appMap[pkg.packageName] = NormalApp(pkg)

                    @SuppressLint("InlinedApi")
                    val uninstalledFlag = if (ApiHelper.hasAndroidN()) PackageManager.MATCH_UNINSTALLED_PACKAGES else PackageManager.GET_UNINSTALLED_PACKAGES
                    appList = ipcFunnel.submit(IPCFunnel.PkgsQuery(request.flags or uninstalledFlag))
                    if (appList == null) throw IPCBufferException("List of installed apps was empty!")
                    for (pkg in appList) {
                        // https://developer.android.com/reference/android/content/pm/PackageManager.html#MATCH_UNINSTALLED_PACKAGES
                        // Note: this flag may cause less information about currently installed applications to be returned.
                        if (!appMap.containsKey(pkg.packageName)) appMap[pkg.packageName] = NormalApp(pkg)
                    }

                    checkForHiddenPackages(appMap, request)

                    cachedRequest = CachedRequest(appMap)
                    requestCache[request] = cachedRequest!!
                }
            }
        }
        return cachedRequest!!.data
    }

    private fun checkForHiddenPackages(appMap: MutableMap<String, PkgInfo>, request: AppsRequest) {
        val rootContext = rootManager.rootContext.blockingFirst()
        val hiddenPackages = ArrayList<InstantApp>()
        if (ApiHelper.hasOreo() && rootContext.isRooted) {
            val result = Cmd.builder("pm list packages -f").execute(RxCmdShell.builder().root(true).build())
            Timber.tag(TAG).d("Result: %s", result)

            for (s in result.output) {
                val matcher = pattern.matcher(s)
                if (matcher.matches()) {
                    val pkgName = matcher.group(2)
                    if (appMap.containsKey(pkgName)) continue

                    val sourcePath = matcher.group(1)
                    val pkgInfo = ipcFunnel.submit(object : IPCFunnel.PMQuery<PackageInfo> {
                        override fun onPackManAction(pm: PackageManager): PackageInfo? {
                            return pm.getPackageArchiveInfo(sourcePath, request.flags)
                        }
                    })
                    if (pkgInfo != null) {
                        val app = InstantApp(pkgInfo, sourcePath)
                        hiddenPackages.add(app)
                    }
                }
            }

        }

        if (hiddenPackages.isNotEmpty()) {
            val dest = File(context.cacheDir, "packages.xml-" + UUID.randomUUID().toString())
            try {
                val cpResult = Cmd.builder(
                        "cp /data/system/packages.xml " + dest.absolutePath,
                        "chown -R `stat " + dest.parent + " -c %u:%g` " + dest.absolutePath
                ).execute(RxCmdShell.builder().root(true).build())

                if (cpResult.exitCode == Cmd.ExitCode.OK) {
                    val docFactory = DocumentBuilderFactory.newInstance()
                    val docBuilder = docFactory.newDocumentBuilder()

                    val inputSource = InputSource(FileReader(dest))
                    val document = docBuilder.parse(inputSource)

                    if (document != null) {
                        for (instantApp in hiddenPackages) {
                            updateHiddenPackage(instantApp, document)
                        }
                    }
                }

            } catch (e: Exception) {
                Timber.tag(TAG).e(e)
            } finally {
                val deleted = dest.delete()
                Timber.tag(TAG).d("Temp file deletion (success=%b, path=%s", deleted, dest)
            }
        }

        for (pkgInfo in hiddenPackages) {
            appMap[pkgInfo.packageName] = pkgInfo
        }
    }

    private fun updateHiddenPackage(pkgInfo: InstantApp, document: Document) {
        val root = document.documentElement ?: return

        val children = root.childNodes
        for (i in 0 until children.length) {
            if (children.item(i) == null) continue
            if (children.item(i).nodeName == "package") {
                val entry = children.item(i) as Element
                if (pkgInfo.packageName != entry.getAttribute("name")) continue

                if (entry.hasAttribute("ut")) {
                    pkgInfo.lastUpdateTime = java.lang.Long.parseLong(entry.getAttribute("ut"), 16)
                }
                if (entry.hasAttribute("it")) {
                    pkgInfo.firstInstallTime = java.lang.Long.parseLong(entry.getAttribute("it"), 16)
                }
            }
        }
    }

}
