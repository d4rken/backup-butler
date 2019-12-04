package eu.darken.bb.processor.core.mm

import com.squareup.moshi.Moshi
import eu.darken.bb.App
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.file.core.local.listFilesThrowing
import eu.darken.bb.common.moshi.from
import eu.darken.bb.common.moshi.into
import okio.Sink
import okio.Source
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import kotlin.concurrent.thread

@PerApp
class MMDataRepo @Inject constructor(
        @CachePath private val cachePath: File,
        moshi: Moshi
) {

    private val tmpDir: File = File(cachePath, CACHEDIR)
    private val refMap = mutableMapOf<Backup.Id, MutableList<MMRef>>()
    private val propsAdapter = moshi.adapter(Props::class.java)

    init {
        if (tmpDir.mkdirs()) {
            Timber.tag(TAG).d("TMP dirs created: %s", tmpDir)
        } else {
            thread(start = true, name = "MMRepo Cleanup") {
                tmpDir.listFilesThrowing().forEach {
                    it.deleteRecursively()
                }
            }
        }
    }

    @Synchronized
    fun create(request: MMRef.Request): MMRef {
        val refId = MMRef.Id()

        val ref = MMRef(
                refId = refId,
                backupId = request.backupId,
                source = request.source
        )

        refMap.getOrPut(request.backupId, { mutableListOf() }).add(ref)

        return ref
    }

    fun readProps(input: Source): Props = propsAdapter.from(input)

    fun writeProps(props: Props, output: Sink) = propsAdapter.into(props, output)

    @Synchronized
    fun release(backupId: Backup.Id) {
        Timber.tag(TAG).d("release(%s): %s", backupId, refMap[backupId])
        refMap[backupId]?.forEach { it ->
            it.source.release()
        }
        refMap.remove(backupId)
    }

    @Synchronized
    fun releaseAll() {
        Timber.tag(TAG).d("Releasing all refs.")
        refMap.keys.toList().forEach {
            release(it)
        }
    }

    companion object {
        val TAG = App.logTag("MMDataRepo")
        const val CACHEDIR = "mmdatarepo"
    }
}