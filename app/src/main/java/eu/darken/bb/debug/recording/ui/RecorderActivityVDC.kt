package eu.darken.bb.debug.recording.ui


import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.ShareBuilder
import eu.darken.bb.common.Stater
import eu.darken.bb.common.Zipper
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.file.JavaFile
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.task.ui.editor.intro.IntroFragmentVDC
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Provider

class RecorderActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val recordedPath: String,
        @AppContext private val context: Context,
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
                    shareBuilderProvider.get().file(JavaFile.build(it.first)).start()
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

    @AssistedInject.Factory
    interface Factory : VDCFactory<IntroFragmentVDC> {
        fun create(handle: SavedStateHandle, recordedPath: String): RecorderActivityVDC
    }

}