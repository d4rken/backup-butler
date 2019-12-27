package eu.darken.bb.common.pkgs.pkgops.installer

import android.content.Context
import android.content.Intent
import eu.darken.bb.App
import eu.darken.bb.BuildConfig
import eu.darken.bb.R
import eu.darken.bb.common.HotData
import eu.darken.bb.common.SharedHolder
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.files.core.local.root.DetailedInputSource
import eu.darken.bb.common.files.core.local.root.DetailedInputSourceWrap
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import eu.darken.bb.common.pkgs.pkgops.installer.InstallerReceiver.InstallEvent
import eu.darken.bb.common.pkgs.pkgops.installer.routine.DefaultRoutine
import eu.darken.bb.common.pkgs.pkgops.root.PkgOpsClient
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.common.root.core.javaroot.JavaRootClient
import eu.darken.bb.common.update
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.task.core.results.LogEvent
import io.reactivex.Observable
import timber.log.Timber
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.inject.Inject

@PerApp
class APKInstaller @Inject constructor(
        @AppContext private val context: Context,
        private val javaRootClient: JavaRootClient,
        private val pkgOps: PkgOps
) : Progress.Client, Progress.Host, SharedHolder.HasKeepAlive<Any> {

    private val progressPub = HotData(Progress.Data())
    override val progress: Observable<Progress.Data> = progressPub.data
    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)

    override val keepAlive = SharedHolder.createKeepAlive(TAG)

    private val installer = context.packageManager.packageInstaller
    private val installMap = mutableMapOf<String, OnGoingInstall>()

    data class OnGoingInstall(
            val semaphore: Semaphore,
            val rootInstall: Boolean = false,
            val installResult: InstallEvent? = null
    )

    data class Request(
            val packageName: String,
            val baseApk: MMRef,
            val splitApks: List<MMRef>,
            val useRoot: Boolean
    )

    data class Result(
            val success: Boolean,
            val error: Exception? = null
    )

    fun install(request: Request, logListener: ((LogEvent) -> Unit)? = null): Result {
        Timber.tag(TAG).d("install(request=%s)", request)
        updateProgressPrimary(R.string.progress_restoring_apk)
        updateProgressSecondary(R.string.progress_working_label)
        updateProgressCount(Progress.Count.Indeterminate())

        val callbackLock = Semaphore(0)
        synchronized(installMap) {
            require(installMap[request.packageName] == null) {
                "Already installing ${request.packageName}"
            }
            installMap[request.packageName] = OnGoingInstall(callbackLock, true)
        }

        val apkInputs = mutableListOf<DetailedInputSource>()
        try {
            apkInputs.add(DetailedInputSourceWrap(
                    request.baseApk.props.originalPath as LocalPath,
                    request.baseApk.source.open()
            ))

            request.splitApks
                    .map {
                        DetailedInputSourceWrap(
                                it.props.originalPath as LocalPath,
                                it.source.open()
                        )
                    }
                    .forEach { apkInputs.add(it) }

            val remoteRequest = object : RemoteInstallRequest.Stub() {
                override fun getPackageName(): String = request.packageName

                override fun getApkInputs(): List<DetailedInputSource> = apkInputs
            }

            if (request.useRoot) {
                javaRootClient.keepAliveWith(this)
                javaRootClient.runModuleAction(PkgOpsClient::class.java) {
                    it.install(remoteRequest)
                }
            } else {
                DefaultRoutine(context).install(remoteRequest)
            }

        } finally {
            apkInputs.forEach {
                try {
                    it.input().close()
                } catch (e: Exception) {
                    Timber.tag(TAG).w(e, "Failed to close remote input stream for ${it.path()}")
                }
            }
        }

        updateProgressSecondary(R.string.progress_waiting_on_install)
        Timber.tag(TAG).d("Waiting for PackageInstaller callback for %s", request.packageName)
        val noTimeout = callbackLock.tryAcquire(1, 120, TimeUnit.SECONDS)

        val ongoingInstall = installMap.remove(request.packageName)
        requireNotNull(ongoingInstall) { "No OnGoingInstall found for $request" }

        var success = false
        var error: Exception? = null
        if (!noTimeout) {
            error = TimeoutException("Installer did not finish in time: $request")
        } else {
            success = ongoingInstall.installResult!!.code == InstallEvent.Code.SUCCESS
            if (!success) {
                error = IllegalStateException(ongoingInstall.installResult.statusMessage)
            }
        }

        if (success) {
            logListener?.let { listener ->
                val dest = pkgOps.queryAppInfos(request.packageName)!!
                listener(LogEvent(LogEvent.Type.RESTORED, LocalPath.build(dest.sourceDir)))
                dest.splitSourceDirs?.forEach {
                    listener(LogEvent(LogEvent.Type.RESTORED, LocalPath.build(it)))
                }
            }
        }

        return Result(success = success, error = error)
    }

    fun handleEvent(event: InstallEvent) = when (event.code) {
        InstallEvent.Code.SUCCESS, InstallEvent.Code.ERROR -> {
            requireNotNull(event.sessionId) { "Event has no packagename: $event" }
            synchronized(installMap) {
                var pkgName = event.packageName
                if (pkgName == null) {
                    Timber.tag(TAG).d("Event had no package name, trying lookup via session id.")
                    requireNotNull(event.sessionId) { "Event had no session ID and no package name, wtf." }
                    val session = installer.allSessions.find { it.sessionId == event.sessionId }
                    requireNotNull(session) { "Can't find matching session for ${event.sessionId}: ${installer.allSessions}" }
                    pkgName = session.appPackageName!!
                }
                installMap.update(pkgName) { it?.copy(installResult = event) }
                installMap.getValue(pkgName).semaphore.release()
            }
        }
        InstallEvent.Code.USER_ACTION -> {
            val actionIntent = event.userAction!!
            actionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(actionIntent)
        }
    }


    companion object {
        val TAG = App.logTag("Installer")

        internal fun createAction(packageName: String): String {
            return "${BuildConfig.APPLICATION_ID}.INSTALLER.CALLBACK:$packageName"
        }
    }
}