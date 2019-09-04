package eu.darken.bb.processor.core.mm

import android.content.Context
import eu.darken.bb.App
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.file.SFile
import eu.darken.bb.common.file.deleteAll
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@PerApp
class MMDataRepo @Inject constructor(
        @AppContext private val context: Context
) {
    private val tmpDir: File = File(context.externalCacheDir, "tmprepo")
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
                type = props.refType,
                originalPath = props.originalPath
        )

        refMap.getOrPut(backupId, { mutableListOf() }).add(ref)

        return ref
    }

    @Synchronized
    fun create(backupId: Backup.Id, orig: SFile): MMRef {
        val refId = MMRef.Id()

        val ref = MMRef(
                refId = refId,
                backupId = backupId,
                type = orig.type.toMMRefType(),
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
                    Timber.tag(TAG).v("Delete tmp file (success=%b): %s", deleted, it.tmpPath)
                }
                MMRef.Type.DIRECTORY -> {
                    val deleted = it.tmpPath.deleteAll()
                    Timber.tag(TAG).v("Delete tmp dir (success=%b): %s", deleted, it.tmpPath)
                }
            }
        }
    }

    @Synchronized
    fun wipe() {
        Timber.tag(TAG).d("Wiping remaining ref cache.")
        refMap.keys.forEach {
            deleteAll(it)
        }
    }

    companion object {
        val TAG = App.logTag("TmpDataRepo")
    }
}