package eu.darken.bb.processor.core.mm

import eu.darken.bb.App
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.file.APath
import eu.darken.bb.common.file.deleteAll
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@PerApp
class MMDataRepo @Inject constructor(
        @CachePath private val cachePath: File
) {

    private val tmpDir: File = File(cachePath, CACHEDIR)
    private val refMap = mutableMapOf<Backup.Id, MutableList<MMRef>>()

    init {
        if (tmpDir.mkdirs()) {
            Timber.tag(TAG).d("TMP dirs created: %s", tmpDir)
        }
    }

    @Synchronized
    fun create(backupId: Backup.Id, props: MMRef.Props): MMRef {
        val refId = MMRef.Id()

        val ref = MMRef(
                backupId = backupId,
                refId = refId,
                tmpPath = File(tmpDir, refId.idString),
                originalPath = props.originalPath
        )

        refMap.getOrPut(backupId, { mutableListOf() }).add(ref)

        return ref
    }

    @Synchronized
    fun create(backupId: Backup.Id, orig: APath): MMRef {
        val refId = MMRef.Id()

        val ref = MMRef(
                refId = refId,
                backupId = backupId,
                tmpPath = File(tmpDir, refId.idString),
                originalPath = orig
        )

        refMap.getOrPut(backupId, { mutableListOf() }).add(ref)

        return ref
    }

    @Synchronized
    fun deleteAll(backupId: Backup.Id) {
        Timber.tag(TAG).d("deleteAll(%s): %s", backupId, refMap[backupId])
        refMap[backupId]?.forEach { it ->
            when (it.type) {
                MMRef.Type.FILE -> {
                    val deleted = it.tmpPath.delete()
                    Timber.tag(TAG).v("Deleted tmp file (success=%b): %s", deleted, it.tmpPath)
                }
                MMRef.Type.DIRECTORY -> {
                    val deleted = it.tmpPath.deleteAll()
                    Timber.tag(TAG).v("Deleted tmp dir (success=%b): %s", deleted, it.tmpPath)
                }
                MMRef.Type.UNUSED -> Timber.tag(TAG).e("Unused ref: %s", it)
            }
        }
        refMap.remove(backupId)
    }

    @Synchronized
    fun wipe() {
        Timber.tag(TAG).d("Wiping remaining ref cache.")
        refMap.keys.toList().forEach {
            deleteAll(it)
        }
    }

    companion object {
        val TAG = App.logTag("TmpDataRepo")
        const val CACHEDIR = "mmdatarepo"
    }
}