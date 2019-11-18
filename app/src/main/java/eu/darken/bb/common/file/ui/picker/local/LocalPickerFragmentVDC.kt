package eu.darken.bb.common.file.ui.picker.local

import android.os.Environment
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.file.core.APath
import eu.darken.bb.common.file.core.APathLookup
import eu.darken.bb.common.file.core.WriteException
import eu.darken.bb.common.file.core.local.LocalGateway
import eu.darken.bb.common.file.core.local.LocalPath
import eu.darken.bb.common.file.core.local.toCrumbs
import eu.darken.bb.common.file.ui.picker.APathPicker
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import java.util.*

class LocalPickerFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val options: APathPicker.Options,
        private val localGateway: LocalGateway
) : SmartVDC() {

    private var sessionSub: Disposable = Disposables.disposed()
    private val startPath = options.startPath as? LocalPath
    private val mode = LocalGateway.Mode.valueOf(options.payload.getString(ARG_MODE, LocalGateway.Mode.AUTO.name))

    private val stater = Stater {
        val (path, crumbs, listing) = doCd(startPath)
        State(
                currentPath = path,
                currentListing = listing,
                currentCrumbs = crumbs,
                allowCreateDir = true
        )
    }
    val state = stater.liveData

    val createDirEvent = SingleLiveEvent<APath>()
    val resultEvents = SingleLiveEvent<APathPicker.Result>()
    val errorEvents = SingleLiveEvent<Throwable>()


    override fun onCleared() {
        sessionSub.dispose()
        super.onCleared()
    }

    private fun doCd(_path: LocalPath?): Triple<LocalPath, List<LocalPath>, List<APathLookup>> {
        // TODO cleaner keep alive?
        if (sessionSub.isDisposed) sessionSub = localGateway.session.subscribe()

        val path = _path ?: LocalPath.build(Environment.getExternalStorageDirectory())
        val listing = localGateway.lookupFiles(path, mode = mode)
                .sortedBy { it.name.toLowerCase(Locale.ROOT) }
                .filter { !options.onlyDirs || it.isDirectory }
        val crumbs = path.toCrumbs()
        return Triple(path, crumbs, listing)
    }

    fun selectItem(selected: APathLookup?) = selectItem(selected?.lookedUp)

    fun selectItem(selected: APath?) {
        Observable
                .fromCallable { doCd(selected as LocalPath) }
                .subscribeOn(Schedulers.io())
                .subscribe({ (path, crumbs, listing) ->
                    stater.update { state ->
                        state.copy(
                                currentPath = path,
                                currentListing = listing,
                                currentCrumbs = crumbs
                        )
                    }
                }, {
                    errorEvents.postValue(it)
                })
    }

    fun createDirRequest() {
        val current = stater.snapshot.currentPath
        createDirEvent.postValue(current)
    }

    fun createDir(name: String) {
        stater.data.take(1)
                .map {
                    val current = it.currentPath
                    val child = current.child(name) as LocalPath
                    if (localGateway.createDir(child, mode = mode)) {
                        child
                    } else {
                        throw WriteException(child)
                    }
                }
                .subscribe({
                    selectItem(it)
                }, {
                    errorEvents.postValue(it)
                })
    }

    fun finishSelection() {
        val selected = setOf(stater.snapshot.currentPath)
        val result = APathPicker.Result(
                options = options,
                selection = selected
        )
        resultEvents.postValue(result)
    }

    fun goHome() {
        selectItem(startPath)
    }

    data class State(
            val currentPath: APath,
            val currentCrumbs: List<APath>,
            val currentListing: List<APathLookup>,
            val allowCreateDir: Boolean = false
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<LocalPickerFragmentVDC> {
        fun create(handle: SavedStateHandle, options: APathPicker.Options): LocalPickerFragmentVDC
    }

    companion object {
        val TAG = App.logTag("Picker", "Local", "VDC")
        val ARG_MODE = "picker.local.mode"
    }
}