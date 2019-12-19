package eu.darken.bb.backup.core.app.restore

import android.content.Context
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.backup.core.app.AppBackupSpec
import eu.darken.bb.backup.core.app.AppBackupWrapper
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.HotData
import eu.darken.bb.common.SharedHolder
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.files.core.*
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.hasCause
import eu.darken.bb.common.pkgs.AppPkg
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import eu.darken.bb.common.pkgs.pkgops.installer.APKInstaller
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.common.root.core.javaroot.JavaRootClient
import eu.darken.bb.common.root.core.javaroot.RootUnavailableException
import io.reactivex.Observable
import okio.source
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class AppRestoreEndpoint @Inject constructor(
        @AppContext override val context: Context,
        private val javaRootClient: JavaRootClient,
        private val apkInstaller: APKInstaller,
        private val gatewaySwitch: GatewaySwitch,
        private val pkgOps: PkgOps
) : Restore.Endpoint, Progress.Client, HasContext {

    private val progressPub = HotData(Progress.Data())
    override val progress: Observable<Progress.Data> = progressPub.data
    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)

    private var rootToken: SharedHolder.Resource<JavaRootClient.Connection>? = null
    private var rootAvailable = true

    override fun restore(config: Restore.Config, backup: Backup.Unit): Boolean {
        updateProgressPrimary(R.string.progress_restoring_backup)
        updateProgressSecondary("")
        updateProgressCount(Progress.Count.Indeterminate())

        config as AppRestoreConfig
        val spec = backup.spec as AppBackupSpec
        val handler = AppBackupWrapper(backup)

        updateProgressCount(Progress.Count.Counter(0, handler.data.size))

        if (config.skipExistingApps && pkgOps.queryPkg(spec.packageName) != null) {
            // TODO skip if pkg already exists?, result?
        }

        if (rootAvailable && rootToken == null) {
            try {
                rootToken = javaRootClient.client.get()
            } catch (e: Exception) {
                if (e.hasCause(RootUnavailableException::class)) rootAvailable = false
                else throw e
            }
        }

        val request = APKInstaller.Request(
                packageName = handler.packageName,
                baseApk = handler.baseApk,
                splitApks = handler.splitApks.toList(),
                useRoot = rootAvailable
        )
        // TODO check result, error?
        val installResult = apkInstaller.install(request)
        // TODO if we don't restore the APK and it's not installed then we can't restore data, error? log? result?

        val pkg = pkgOps.queryPkg(handler.packageName)
        requireNotNull(pkg) { "${handler.packageName} isn't installed." }

        val appInfo = (pkg as? AppPkg)?.applicationInfo
        requireNotNull(appInfo) { "${pkg.packageType} is currently not supported." }

        if (config.restoreData) {
            // TODO Copy private data
            val privateData = handler.dataPrivate
            val archive = privateData.single()

            val props = archive.props

            val directoryTimeStamps = mutableMapOf<APath, Date>()

            archive.source.open().use { source ->
                val archiveStream = TarArchiveInputStream(GzipCompressorInputStream(source.inputStream()))

                generateSequence { archiveStream.nextTarEntry }.forEach { entry ->
                    val entryOrigPath = entry.name
                    Timber.tag(TAG).v("Restoring: %s", entryOrigPath)

                    props.originalPath
                    val restoreTarget = LocalPath.build("/data/data/abnbtest", entryOrigPath)

                    // TODO what if the file exists?
                    when {
                        entry.isSymbolicLink -> {

                        }
                        entry.isDirectory -> {
                            restoreTarget.createDirIfNecessary(gatewaySwitch)
                        }
                        entry.isFile -> {
                            restoreTarget.createFileIfNecessary(gatewaySwitch)
                            archiveStream.source().constrain(entry.size).use { fileSource ->
                                restoreTarget.write(gatewaySwitch).use {
                                    fileSource.copyToAutoClose(it)
                                }
                            }
                        }
                        else -> throw UnsupportedOperationException("Unknown type for ${entry.name}")
                    }

                    val modifiedTime = entry.lastModifiedDate
                    if (entry.isDirectory) {
                        directoryTimeStamps[restoreTarget] = modifiedTime
                    } else {
                        restoreTarget.setModifiedAt(gatewaySwitch, modifiedTime)
                    }

                    val permissions = Permissions(entry.mode)
                    restoreTarget.setPermissions(gatewaySwitch, permissions)

                    val targetUID = appInfo.uid
                    var targetGID: Int? = null

                    val oldOwnership = entry.getOwnership()
                    if (oldOwnership?.groupName?.endsWith("_cache") == true) {
                        val targetUIDName = pkgOps.getUserNameForUID(targetUID)
                        val targetGIDName = targetUIDName + "_cache"
                        targetGID = pkgOps.getGIDForGroupName(targetGIDName)
                    }
                    if (targetGID == null) targetGID = targetUID

                    restoreTarget.setOwnership(gatewaySwitch, Ownership(targetUID, targetGID))
                }
            }

            Timber.tag(TAG).v("Setting timestamps for %d directories.", directoryTimeStamps.size)
            directoryTimeStamps.forEach { (path, lastModified) ->
                path.setModifiedAt(gatewaySwitch, lastModified)
            }


        }

        // TODO Copy private cache

        // TODO Copy public data

        // TODO Copy public cache

        // TODO Copy public clutter


        // TODO return result?
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