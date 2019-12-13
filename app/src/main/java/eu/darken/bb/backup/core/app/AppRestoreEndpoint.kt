package eu.darken.bb.backup.core.app

import android.content.Context
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.HotData
import eu.darken.bb.common.SharedResource
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.hasCause
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.common.root.core.javaroot.JavaRootClient
import eu.darken.bb.common.root.core.javaroot.RootUnavailableException
import eu.darken.bb.common.root.core.javaroot.pkgops.Installer
import io.reactivex.Observable
import javax.inject.Inject

class AppRestoreEndpoint @Inject constructor(
        @AppContext override val context: Context,
        private val installer: Installer,
        private val javaRootClient: JavaRootClient
) : Restore.Endpoint, Progress.Client, HasContext {

    private val progressPub = HotData(Progress.Data())
    override val progress: Observable<Progress.Data> = progressPub.data
    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)

    private var rootToken: SharedResource.Resource<JavaRootClient.Connection>? = null
    private var rootAvailable = true

    override fun restore(config: Restore.Config, backup: Backup.Unit): Boolean {
        updateProgressPrimary(R.string.progress_restoring_backup)
        updateProgressSecondary("")
        updateProgressCount(Progress.Count.Indeterminate())

        config as AppRestoreConfig
        val spec = backup.spec as AppBackupSpec
        val handler = AppBackupWrapper(backup)

        updateProgressCount(Progress.Count.Counter(0, handler.data.size))

        // TODO skip if pkg already exists?


        if (rootAvailable && rootToken == null) {
            try {
                rootToken = javaRootClient.sharedResource.get()
            } catch (e: Exception) {
                if (e.hasCause(RootUnavailableException::class)) rootAvailable = false
                else throw e
            }
        }

        // TODO Restore APK file first, if enabled
        // Copy APK to public cache folder
        // Launch root APK install if available
        // Launch Install Dialog if root is not available


        // TODO Install APK
        val request = Installer.Request(
                packageName = handler.packageName,
                baseApk = handler.baseApk,
                splitApks = handler.splitApks.toList(),
                useRoot = rootAvailable
        )
        installer.install(request)


        // TODO if we don't restore the APK and it's not installed then we can't restore data, error? log? result?

        // TODO Copy private data

        // TODO Copy private cache

        // TODO Copy public data

        // TODO Copy public cache

        // TODO Copy public clutter


        return true
    }

    override fun close() {
        rootToken?.close()
//        TODO("not implemented")
    }

    override fun toString(): String = "AppEndpoint()"

    companion object {
        val TAG = App.logTag("AppBackup", "Endpoint")
    }

}