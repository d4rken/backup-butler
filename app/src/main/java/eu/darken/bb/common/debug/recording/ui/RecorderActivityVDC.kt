package eu.darken.bb.common.debug.recording.ui


import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.ShareBuilder
import eu.darken.bb.common.Zipper
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.RawPath
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.flow.onError
import eu.darken.bb.common.flow.replayingShare
import eu.darken.bb.common.smart.Smart2VDC
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

@HiltViewModel
class RecorderActivityVDC @Inject constructor(
    handle: SavedStateHandle,
    private val shareBuilderProvider: Provider<ShareBuilder>,
    private val dispatcherProvider: DispatcherProvider
) : Smart2VDC(dispatcherProvider) {

    private val recordedPath = handle.get<String>(RecorderActivity.RECORD_PATH)!!
    private val pathCache = MutableStateFlow(recordedPath)
    private val resultCacheObs = pathCache
        .map { path -> Pair(path, File(path).length()) }
        .replayingShare(vdcScope)

    private val resultCacheCompressedObs = resultCacheObs
        .map { uncompressed ->
            val zipped = "${uncompressed.first}.zip"
            Zipper().zip(arrayOf(uncompressed.first), zipped)
            Pair(zipped, File(zipped).length())
        }
        .replayingShare(vdcScope + dispatcherProvider.IO)

    private val stater = DynamicStateFlow(TAG, vdcScope) { State() }

    val state = stater.asLiveData2()

    init {
        resultCacheObs
            .onEach { (path, size) ->
                stater.updateBlocking { copy(normalPath = path, normalSize = size) }
            }
            .launchInViewModel()

        resultCacheCompressedObs
            .onEach { (path, size) ->
                stater.updateBlocking {
                    copy(
                        compressedPath = path,
                        compressedSize = size,
                        loading = false
                    )
                }
            }
            .onError { error ->
                stater.updateBlocking { copy(error = error) }
            }
            .launchInViewModel()

    }

    fun share() = launch {
        resultCacheCompressedObs.collect {
            shareBuilderProvider.get().file(RawPath.build(it.first)).start()
        }
    }

    data class State(
        val normalPath: String? = null,
        val normalSize: Long = -1L,
        val compressedPath: String? = null,
        val compressedSize: Long = -1L,
        val error: Throwable? = null,
        val loading: Boolean = true
    )

    companion object {
        private val TAG = logTag("Debug", "Recorder", "VDC")
    }
}