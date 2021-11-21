package eu.darken.bb.storage.core.local

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.*
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.Logging.Priority.ERROR
import eu.darken.bb.common.debug.logging.Logging.Priority.WARN
import eu.darken.bb.common.debug.logging.asLog
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.ReadException
import eu.darken.bb.common.files.core.asFile
import eu.darken.bb.common.files.core.copyToAutoClose
import eu.darken.bb.common.files.core.local.*
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.flow.onError
import eu.darken.bb.common.flow.onErrorMixLast
import eu.darken.bb.common.flow.replayingShare
import eu.darken.bb.common.moshi.fromFile
import eu.darken.bb.common.moshi.toFile
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.common.sharedresource.SharedResource
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.MMRef.Type.*
import eu.darken.bb.processor.core.mm.archive.ArchiveProps
import eu.darken.bb.processor.core.mm.archive.ArchiveRefSource
import eu.darken.bb.processor.core.mm.generic.GenericRefSource
import eu.darken.bb.storage.core.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.plus
import kotlinx.coroutines.yield
import okio.sink
import okio.source
import timber.log.Timber
import java.io.File
import java.io.InterruptedIOException


class LocalStorage @AssistedInject constructor(
    @Assisted storageRef: Storage.Ref,
    @Assisted storageConfig: Storage.Config,
    @ApplicationContext override val context: Context,
    moshi: Moshi,
    private val mmDataRepo: MMDataRepo,
    private val localGateway: LocalGateway,
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
) : Storage, HasContext, Progress.Client {

    override val sharedResource = SharedResource.createKeepAlive(TAG, appScope + dispatcherProvider.IO)

    private val storageRef: LocalStorageRef = storageRef as LocalStorageRef
    override val storageConfig: LocalStorageConfig = storageConfig as LocalStorageConfig
    private val dataDir = File(this.storageRef.path.asFile(), "data")

    private val specAdapter = moshi.adapter(BackupSpec::class.java)
    private val metaDataAdapter = moshi.adapter(Backup.MetaData::class.java)

    private val progressPub = DynamicStateFlow(TAG, appScope) { Progress.Data() }
    override val progress: Flow<Progress.Data> = progressPub.flow
    override fun updateProgress(update: suspend (Progress.Data) -> Progress.Data) = progressPub.updateAsync(onUpdate = update)

    private val dataDirEvents: Flow<List<File>> = flow {
        // FIXME better way to update listing
        while (true) {
            val listing = try {
                dataDir.listFilesThrowing()
            } catch (e: Exception) {
                emptyList()
            }
            emit(listing)
            delay(1000)
            yield()
        }
    }
        .distinctUntilChanged()
        .replayingShare(appScope)

    init {
        Timber.tag(TAG).i("init(storageRef=%s, storageConfig=%s)", storageRef, storageConfig)
        if (!dataDir.exists()) {
            Timber.tag(TAG).w("Data dir doesn't exist: %s", dataDir)
        }
    }

    override fun info(): Flow<Storage.Info> = infowFlow
    private val infowFlow: Flow<Storage.Info> =
        flowOf(Storage.Info(this.storageRef.storageId, this.storageRef.storageType, storageConfig))
            .flatMapConcat { info ->
                specInfos()
                    .map { contents ->
                        var status: Storage.Info.Status? = null
                        try {
                            status = Storage.Info.Status(
                                itemCount = contents.size,
                                totalSize = 0,
                                isReadOnly = dataDir.exists() && !dataDir.canWrite()
                            )
                        } catch (e: Exception) {
                            Timber.tag(TAG).w(e)
                        }

                        info.copy(status = status)
                    }
                    .onStart { emit(info) }
            }
            .onError { Timber.tag(TAG).e(it) }
            .onStart { Timber.tag(TAG).d("info().doOnSubscribe()") }
            .onCompletion { Timber.tag(TAG).d("info().doFinally()") }
            .onErrorMixLast { last, error ->
                // startWith
                last!!.copy(error = error)
            }
            .replayingShare(appScope)

    override fun specInfo(specId: BackupSpec.Id): Flow<BackupSpec.Info> = specInfos()
        .map { specInfos ->
            val item = specInfos.find { it.backupSpec.specId == specId }
            requireNotNull(item) { "Can't find backup item for specId $specId" }
        }

    override fun specInfos(): Flow<Collection<BackupSpec.Info>> = itemObs
    private val itemObs: Flow<Collection<BackupSpec.Info>> = dataDirEvents
        .map { files ->
            val content = mutableListOf<BackupSpec.Info>()

            for (backupDir in files) {
                if (backupDir.isFile) {
                    Timber.tag(TAG).w("Unexpected file within data directory: %s", backupDir)
                    continue
                }

                val backupConfig = try {
                    readSpec(BackupSpec.Id(backupDir.name))
                } catch (e: Throwable) {
                    if (e is InterruptedIOException) throw e
                    Timber.tag(TAG).w("Dir without spec file: %s", backupDir)
                    continue
                }

                val metaDatas = getMetaDatas(backupConfig.specId)
                if (metaDatas.isEmpty()) {
                    Timber.tag(TAG).w("Dir without backups? %s", backupDir)
                    continue
                }

                val ref = LocalStorageSpecInfo(
                    storageId = storageConfig.storageId,
                    path = backupDir.asSFile(),
                    backupSpec = backupConfig,
                    backups = metaDatas
                )
                content.add(ref)
            }
            content
        }
        .map { it }
        .onError { log(TAG, ERROR) { "specInfos() failed: ${it.asLog()}" } }
        .onStart { Timber.tag(TAG).d("doOnSubscribe().doFinally()") }
        .onCompletion { Timber.tag(TAG).d("specInfos().doFinally()") }
        .replayingShare(appScope)

    override fun backupInfo(specId: BackupSpec.Id, backupId: Backup.Id): Flow<Backup.Info> =
        backupContent(specId, backupId)
            .map { Backup.Info(it.storageId, it.spec, it.metaData) }

    override fun backupContent(specId: BackupSpec.Id, backupId: Backup.Id): Flow<Backup.ContentInfo> =
        specInfo(specId)
            .flatMapConcat { item ->
                item as LocalStorageSpecInfo
                val backupDir = item.path.asFile().requireExists()
                val versionDir = File(backupDir, backupId.idString).requireExists()
                val backupSpec = readSpec(specId)
                val metaData = readBackupMeta(item.specId, backupId)

                return@flatMapConcat flow<List<File>> {

                    while (true) {
                        emit(backupDir.listFilesThrowing())
                        delay(1000L)
                        yield()
                    }
                }
                    .distinctUntilChanged()
                    .map {
                        return@map versionDir.listFilesThrowing()
                            .filter { it.path.endsWith(PROP_EXT) }
                            .map { file ->
                                val props = file.source().use { mmDataRepo.readProps(it) }
                                Backup.ContentInfo.PropsEntry(backupSpec, metaData, props)
                            }
                            .toList()
                    }
                    .catch { emit(emptyList()) }
                    .map {
                        return@map Backup.ContentInfo(
                            storageId = storageId,
                            spec = backupSpec,
                            metaData = metaData,
                            items = it
                        )
                    }
            }
            .onStart { Timber.tag(TAG).d("content(%s).doOnSubscribe()", backupId) }
            .onError { log(TAG, WARN) { "Failed to get content: specId=$specId, backupId=$backupId" } }
            .onCompletion { Timber.tag(TAG).d("content(%s).doFinally()", backupId) }
            .replayingShare(appScope)

    override suspend fun load(specId: BackupSpec.Id, backupId: Backup.Id): Backup.Unit {
        updateProgressPrimary(R.string.progress_reading_backup_specs)
        updateProgressCount(Progress.Count.Indeterminate())
        val item = specInfo(specId).first()
        item as LocalStorageSpecInfo

        updateProgressPrimary(R.string.progress_reading_backup_metadata)
        val metaData = readBackupMeta(specId, backupId)

        updateProgressPrimary(R.string.progress_reading_backup_data)
        val dataMap = mutableMapOf<String, MutableList<MMRef>>()
        val versionPath = getVersionDir(specId, backupId).requireExists()
        val propFiles = versionPath.listFilesThrowing().filter { file: File -> file.path.endsWith(PROP_EXT) }
        updateProgressCount(Progress.Count.Percent(0, propFiles.size))

        propFiles.forEachIndexed { index, propFile ->
            updateProgressSecondary(propFile.path)

            val dataFile = File(propFile.parent, propFile.name.replace(PROP_EXT, DATA_EXT))
            val props = propFile.source().use { mmDataRepo.readProps(it) }

            val source: MMRef.RefSource = when (props.dataType) {
                FILE, DIRECTORY, SYMLINK -> GenericRefSource({ dataFile.source() }, { props })
                ARCHIVE -> ArchiveRefSource({ dataFile.source() }, { props as ArchiveProps })
            }

            val tmpRef = mmDataRepo.create(MMRef.Request(backupId = backupId, source = source))

            val keySplit = propFile.name.split("#")
            val key = if (keySplit.size == 2) Base64Tool.decode(keySplit[0]) else ""
            dataMap.getOrPut(key, { mutableListOf() }).add(tmpRef)

            updateProgressCount(Progress.Count.Percent(index + 1, propFiles.size))
        }
        updateProgressSecondary(CaString.EMPTY)
        updateProgressCount(Progress.Count.Indeterminate())

        return Backup.Unit(
            spec = item.backupSpec,
            metaData = metaData,
            data = dataMap
        )
    }

    override suspend fun save(backup: Backup.Unit): Backup.Info {
        updateProgressPrimary(R.string.progress_writing_backup_specs)
        updateProgressCount(Progress.Count.Indeterminate())
        try {
            val existingSpec = readSpec(backup.specId)
            check(existingSpec == backup.spec) { "BackupSpec missmatch:\nExisting: $existingSpec\n\nNew: ${backup.spec}" }
        } catch (e: Throwable) {
            Timber.tag(TAG).d("Reading existing spec failed (${e.message}, creating new one.")
            getSpecDir(backup.specId).tryMkDirs()
            writeSpec(backup.specId, backup.spec)
        }

        updateProgressPrimary(R.string.progress_writing_backup_data)
        val versionDir = getVersionDir(specId = backup.specId, backupId = backup.backupId).tryMkDirs()

        var current = 0
        val max = backup.data.values.fold(0, { cnt, vals -> cnt + vals.size })
        updateProgressCount(Progress.Count.Counter(current, max))

        // TODO guardAction that backup dir doesn't exist, ie version dir?

        backup.data.entries.forEach { (baseKey, refs) ->
            refs.forEach { ref ->

                val key = if (baseKey.isNotBlank()) {
                    val encoded = Base64Tool.encode(baseKey)
                    "$encoded#"
                } else {
                    ""
                }

                val targetProp = File(versionDir, "$key${ref.refId.idString}$PROP_EXT").requireNotExists()
                updateProgressSecondary(targetProp.path)
                targetProp.sink().use { mmDataRepo.writeProps(ref.getProps(), it) }

                // TODO errors should be shown in the result?
                when (ref.getProps().dataType) {
                    FILE, ARCHIVE -> {
                        val target = File(versionDir, "$key${ref.refId.idString}$DATA_EXT").requireNotExists()
                        ref.source.open().copyToAutoClose(target.sink())
                    }
                    DIRECTORY, SYMLINK -> {
                        // NOOP props are enough
                    }
                }

                updateProgressCount(Progress.Count.Counter(++current, max))
            }
        }
        updateProgressSecondary(CaString.EMPTY)
        updateProgressCount(Progress.Count.Indeterminate())

        updateProgressPrimary(R.string.progress_writing_backup_metadata)
        writeBackupMeta(backup.specId, backup.backupId, backup.metaData)
        val info = Backup.Info(
            storageId = storageId,
            spec = backup.spec,
            metaData = backup.metaData
        )
        Timber.tag(TAG).d("New backup created: %s", info)
        return info
    }

    override suspend fun remove(specId: BackupSpec.Id, backupId: Backup.Id?): BackupSpec.Info {
        val specInfo = specInfo(specId).first()
        specInfo as LocalStorageSpecInfo
        if (backupId != null) {
            val versionDir = getVersionDir(specId, backupId)
            versionDir.deleteAll()
        } else {
            val backupDir = getSpecDir(specId)
            backupDir.deleteAll()
        }
        val newMetaData = if (backupId != null) {
            // Not a complete deletion
            getMetaDatas(specId)
        } else {
            emptySet()
        }
        return specInfo.copy(backups = newMetaData)
    }

    override suspend fun detach(wipe: Boolean) {
        Timber.tag(TAG).d("detach(wipe=%b).doOnSubscribe %s", wipe, storageRef)
        if (wipe) {
            Timber.tag(TAG).i("Wiping %s", storageRef.path)
            // let's not use the gateway here as there is currently no reason to run this with root.
            storageRef.path.asFile().deleteAll()
        }
        // TODO dispose resources, i.e. observables.
        Timber.tag(TAG).d("detach(wipe=%b).dofinally %s", wipe, storageRef)
    }


    private fun getSpecDir(specId: BackupSpec.Id): File = File(dataDir, specId.value)

    private fun getVersionDir(specId: BackupSpec.Id, backupId: Backup.Id): File =
        File(getSpecDir(specId), backupId.idString)

    private fun getMetaDatas(specId: BackupSpec.Id): Collection<Backup.MetaData> {
        val metaDatas = mutableListOf<Backup.MetaData>()
        getSpecDir(specId).listFilesThrowing().filter { it.isDirectory }.forEach { dir ->
            try {
                val metaData = readBackupMeta(specId, Backup.Id(dir.name))
                metaDatas.add(metaData)
            } catch (e: ReadException) {
                // TODO How will these get cleaned up?
                log(TAG, ERROR) { "Failed to read meta data for $dir: ${e.asLog()}" }
            }
        }
        return metaDatas
    }

    private fun readBackupMeta(specId: BackupSpec.Id, backupId: Backup.Id): Backup.MetaData {
        val versionFile = File(getVersionDir(specId, backupId), BACKUP_META_FILE)
        return try {
            metaDataAdapter.fromFile(versionFile)
        } catch (e: Exception) {
            log(TAG, WARN) { "Failed to get metadata from $versionFile: ${e.asLog()}" }
            throw e
        }
    }

    private fun writeBackupMeta(specId: BackupSpec.Id, backupId: Backup.Id, metaData: Backup.MetaData) {
        val versionFile = File(getVersionDir(specId, backupId), BACKUP_META_FILE)
        metaDataAdapter.toFile(metaData, versionFile)
    }

    private fun readSpec(specId: BackupSpec.Id): BackupSpec {
        val specFile = File(getSpecDir(specId), SPEC_FILE)
        return try {
            specAdapter.fromFile(specFile)
        } catch (e: Exception) {
            log(TAG, WARN) { "Failed to get  backup spec from $specFile: ${e.asLog()}" }
            throw e
        }
    }

    private fun writeSpec(specId: BackupSpec.Id, spec: BackupSpec) {
        val specFile = File(getSpecDir(specId), SPEC_FILE)
        specAdapter.toFile(spec, specFile)
    }

    override fun toString(): String = "LocalStorage(storageRef=$storageRef)"

    @AssistedFactory
    interface Factory : Storage.Factory<LocalStorage>

    companion object {
        internal val TAG = logTag("Storage", "Local")
        private const val DATA_EXT = ".data"
        private const val PROP_EXT = ".prop.json"
        private const val SPEC_FILE = "spec.json"
        private const val BACKUP_META_FILE = "backup.json"
    }
}