package eu.darken.bb.backup.processor.tmp

import android.content.Context
import eu.darken.bb.App
import eu.darken.bb.AppComponent
import eu.darken.bb.backup.backups.BackupId
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.file.asFile
import eu.darken.bb.common.file.asSFile
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@AppComponent.Scope
class TmpDataRepo @Inject constructor(
        @AppContext private val context: Context
) {
    private val tmpDir: File = File(context.cacheDir, "tmprepo")
    private val refMap = mutableMapOf<BackupId, MutableList<TmpRef>>()

    init {
        if (tmpDir.mkdirs()) {
            Timber.tag(TAG).d("TMP dirs created: %s", tmpDir)
        }
    }

    fun create(backupId: BackupId, type: TmpType = TmpType.FILE): TmpRef {
        val refId = TmpRefId()
        val cacheRef = TmpRef(
                refId = refId,
                backupId = backupId,
                type = type,
                file = File(tmpDir, refId.id.toString()).asSFile()
        )
        if (!refMap.contains(backupId)) {
            refMap[backupId] = mutableListOf()
        }
        refMap[backupId]!!.add(cacheRef)

        return cacheRef
    }

    fun deleteAll(backupId: BackupId) {
        Timber.tag(TAG).d("deleteAll(%s): %s", backupId, refMap[backupId])
        refMap[backupId]?.forEach { it ->
            when (it.type) {
                TmpType.FILE -> {
                    val deleted = it.file.asFile().delete()
                    Timber.tag(TAG).v("Delete tmp file (success=%b): %s", deleted, it.file)
                }
                TmpType.DIRECTORY -> {
                    TODO()
                }
            }
        }
    }

    companion object {
        val TAG = App.logTag("TmpDataRepo")
    }
}