package eu.darken.bb.processor.tmp

import android.content.Context
import eu.darken.bb.App
import eu.darken.bb.backups.core.BackupId
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.file.asFile
import eu.darken.bb.common.file.asSFile
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject

@PerApp
class TmpDataRepo @Inject constructor(
        @AppContext private val context: Context
) {
    private val tmpDir: File = File(context.externalCacheDir, "tmprepo")
    private val refMap = mutableMapOf<BackupId, MutableList<TmpRef>>()

    init {
        if (tmpDir.mkdirs()) {
            Timber.tag(TAG).d("TMP dirs created: %s", tmpDir)
        }
    }

    @Synchronized
    fun create(backupId: BackupId, type: TmpRef.Type = TmpRef.Type.FILE): TmpRef {
        val refId = UUID.randomUUID()
        val cacheRef = TmpRef(
                refId = refId,
                backupId = backupId,
                type = type,
                file = File(tmpDir, refId.toString()).asSFile()
        )
        if (!refMap.contains(backupId)) {
            refMap[backupId] = mutableListOf()
        }
        refMap[backupId]!!.add(cacheRef)

        return cacheRef
    }

    @Synchronized
    fun deleteAll(backupId: BackupId) {
        Timber.tag(TAG).d("deleteAll(%s): %s", backupId, refMap[backupId])
        refMap[backupId]?.forEach { it ->
            when (it.type) {
                TmpRef.Type.FILE -> {
                    val deleted = it.file.asFile().delete()
                    Timber.tag(TAG).v("Delete tmp file (success=%b): %s", deleted, it.file)
                }
                TmpRef.Type.DIRECTORY -> {
                    TODO()
                }
            }
        }
    }

    companion object {
        val TAG = App.logTag("TmpDataRepo")
    }
}