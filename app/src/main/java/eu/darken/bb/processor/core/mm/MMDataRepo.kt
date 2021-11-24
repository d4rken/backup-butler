package eu.darken.bb.processor.core.mm

import com.squareup.moshi.Moshi
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.local.listFiles2
import eu.darken.bb.common.moshi.from
import eu.darken.bb.common.moshi.into
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.Sink
import okio.Source
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MMDataRepo @Inject constructor(
    @CachePath private val cachePath: File,
    moshi: Moshi,
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
) {

    private val tmpDir: File = File(cachePath, CACHEDIR)
    private val refMap = mutableMapOf<Backup.Id, MutableList<MMRef>>()
    private val propsAdapter = moshi.adapter(Props::class.java)
    private val lock = Mutex()

    init {
        if (tmpDir.mkdirs()) {
            Timber.tag(TAG).d("TMP dirs created: %s", tmpDir)
        } else {
            appScope.launch(context = dispatcherProvider.IO) {
                lock.withLock {
                    tmpDir.listFiles2().forEach {
                        log(TAG) { "Cleaning up $it" }
                        it.deleteRecursively()
                    }
                }
            }
        }
    }

    suspend fun create(request: MMRef.Request): MMRef = lock.withLock {
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

    suspend fun release(backupId: Backup.Id) = lock.withLock {
        Timber.tag(TAG).d("release(%s): %s", backupId, refMap[backupId])

        backupId.doRelease()
    }


    suspend fun releaseAll() = lock.withLock {
        Timber.tag(TAG).d("Releasing all refs.")
        refMap.keys.forEach {
            it.doRelease()
        }
    }

    private suspend fun Backup.Id.doRelease() {
        refMap[this]?.forEach { it ->
            it.source.release()
        }
        refMap.remove(this)
    }

    companion object {
        val TAG = logTag("MMDataRepo")
        const val CACHEDIR = "mmdatarepo"
    }
}