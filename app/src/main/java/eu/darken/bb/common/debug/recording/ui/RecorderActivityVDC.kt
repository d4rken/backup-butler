package eu.darken.bb.common.debug.recording.ui


import android.content.Context
import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.ShareBuilder
import eu.darken.bb.common.Stater
import eu.darken.bb.common.Zipper
import eu.darken.bb.common.files.core.RawPath
import eu.darken.bb.common.vdc.VDC
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

@HiltViewModel
class RecorderActivityVDC @Inject constructor(
    private val handle: SavedStateHandle,
    @Assisted private val recordedPath: String,
    @ApplicationContext private val context: Context,
    private val shareBuilderProvider: Provider<ShareBuilder>
) : VDC() {

    private val pathCache = Observable.just(recordedPath)
    private val resultCacheObs = pathCache
        .observeOn(Schedulers.io())
        .map { path -> Pair(path, File(path).length()) }
        .cache()

    private val resultCacheCompressedObs = resultCacheObs
        .observeOn(Schedulers.io())
        .map { uncompressed ->
            val zipped = "${uncompressed.first}.zip"
            Zipper().zip(arrayOf(uncompressed.first), zipped)
            Pair(zipped, File(zipped).length())
        }
        .cache()
    private val stater = Stater(State())

    val state = stater.liveData

    init {
        resultCacheObs.subscribe { (path, size) ->
            stater.update { it.copy(normalPath = path, normalSize = size) }
        }
        resultCacheCompressedObs
            .subscribe(
                { (path, size) ->
                    stater.update {
                        it.copy(
                            compressedPath = path,
                            compressedSize = size,
                            loading = false
                        )
                    }
                },
                { error ->
                    stater.update { it.copy(error = error) }
                }
            )

    }

    fun share() {
        resultCacheCompressedObs
            .subscribe {
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
}