package eu.darken.bb.common.debug.bugsnag


import com.bugsnag.android.Event
import com.bugsnag.android.OnErrorCallback
import eu.darken.bb.BackupButler
import eu.darken.bb.BuildConfig
import eu.darken.bb.GeneralSettings
import eu.darken.bb.common.BBEnv
import eu.darken.bb.common.debug.InstallId
import eu.darken.bb.common.debug.logging.asLog
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BugsnagErrorHandler @Inject constructor(
    private val bbEnv: BBEnv,
    private val installId: InstallId,
    private val bugsnagLogger: BugsnagLogger,
    private val backupButler: BackupButler,
    private val generalSettings: GeneralSettings
) : OnErrorCallback {

    override fun onError(event: Event): Boolean {
        log(TAG) { "Error event: $event\nHandling: ${event.originalError?.asLog()}" }

        bugsnagLogger.injectLog(event)

        event.addMetadata(TAB_APP, "checksumMD5", backupButler.checksumApkMd5)

//        val upgradeControl = sdmContext.getUpgradeControl()
//        error.addToTab(TAB_APP, "upgrades", formatLists(ArrayList(upgradeControl.getUpgradeData().map(???() { UpgradeData.getUpgrades() }).blockingFirst())))
//        error.addToTab(TAB_APP, "debugMode", SDMDebug.INSTANCE.observeOptions().blockingFirst().getLevel())
        event.addMetadata(TAB_APP, "gitSha", BuildConfig.GITSHA)
        event.addMetadata(TAB_APP, "buildTime", BuildConfig.BUILDTIME)

        event.addMetadata(TAB_APP, "signatures", backupButler.signatures.map { it.hashCode() }.toString())

        event.addMetadata(TAB_APP, "updateHistory", backupButler.updateHistory.toString())


//        if (sdmContext.getRootManager().isInitialized()) {
//            val rootContext = sdmContext.getRootManager().getRootContext()
//            error.addToTab(TAB_DEVICE, "jailbroken", rootContext.isRooted())
//            error.addToTab(TAB_ROOTCONTEXT, "rootState", rootContext.getRoot().toString())
//            error.addToTab(TAB_ROOTCONTEXT, "isRootIssue", sdmContext.getRootManager().isRootIssue())
//            error.addToTab(TAB_ROOTCONTEXT, "selinuxState", rootContext.getSELinux().toString())
//            error.addToTab(TAB_ROOTCONTEXT, "subinaryRaw", rootContext.getSuBinary().getRaw())
//            if (rootContext.getSuApp().getPackageName() != null) {
//                val suApp = rootContext.getSuApp()
//                if (suApp.getType() != SuBinary.Type.UNKNOWN && suApp.getType() != SuBinary.Type.NONE) {
//                    error.addToTab(TAB_ROOTCONTEXT, "suType", suApp.getType().name)
//                    error.addToTab(TAB_ROOTCONTEXT, "suAppPackage", suApp.getPackageName())
//                    error.addToTab(TAB_ROOTCONTEXT, "suAppVersionName", suApp.getVersionName())
//                }
//            }
//        }

        return (!BuildConfig.DEBUG && generalSettings.isBugTrackingEnabled).also {
            log(TAG) { "Send error? $it" }
        }
    }

    companion object {
        private val TAG = logTag("Bugsnag", "ErrorHandler")
        private val TAB_APP = "app"
        private val TAB_DEVICE = "device"
        private val TAB_ROOTCONTEXT = "rootcontext"

        private fun getParentClassName(o: Any): String {
            var clazz: Class<*>? = o.javaClass
            while (clazz!!.declaringClass != null) clazz = clazz.declaringClass
            return clazz.simpleName
        }

        private fun formatLists(objects: List<*>): String {
            val sb = StringBuilder("[")
            for (i in objects.indices) {
                sb.append(objects[i].toString())
                if (i + 1 != objects.size) sb.append(", ")
            }
            sb.append("]")
            return sb.toString()
        }
    }
}
