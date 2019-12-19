package eu.darken.bb.common.files.ui.picker.local

import android.Manifest
import android.os.Environment
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.common.RuntimePermissionTool
import eu.darken.bb.common.SharedHolder
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.APathLookup
import eu.darken.bb.common.files.core.WriteException
import eu.darken.bb.common.files.core.local.LocalGateway
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.files.core.local.LocalPathLookup
import eu.darken.bb.common.files.core.local.toCrumbs
import eu.darken.bb.common.files.ui.picker.APathPicker
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.*

class LocalPickerFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val options: APathPicker.Options,
        private val localGateway: LocalGateway,
        permissionTool: RuntimePermissionTool
) : SmartVDC() {
    private val fallbackPath = LocalPath.build(Environment.getExternalStorageDirectory())
    private val startPath = options.startPath as? LocalPath ?: fallbackPath

    private val mode: LocalGateway.Mode by lazy {
        options.payload.classLoader = App::class.java.classLoader
        LocalGateway.Mode.valueOf(options.payload.getString(ARG_MODE, LocalGateway.Mode.AUTO.name))
    }

    private val stater = Stater {
        State(
                currentPath = startPath,
                currentCrumbs = startPath.toCrumbs()
        )
    }
    val state = stater.liveData

    val createDirEvent = SingleLiveEvent<APath>()
    val resultEvents = SingleLiveEvent<APathPicker.Result>()
    val errorEvents = SingleLiveEvent<Throwable>()
    val missingPermissionEvent = SingleLiveEvent<Any>()
    val requestPermissionEvent = SingleLiveEvent<Any>()
    var holderToken: SharedHolder.Resource<*>? = null


    init {
        if (!permissionTool.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            missingPermissionEvent.postValue(Any())
        } else {
            selectItem(startPath)
        }
    }

    override fun onCleared() {
        holderToken?.close()
        super.onCleared()
    }

    fun selectItem(selected: APath?) {
        val unwrapped = when (selected) {
            is LocalPath -> selected
            is LocalPathLookup -> selected.lookedUp
            else -> null
        }

        val doCd: (LocalPath) -> Triple<LocalPath, List<LocalPath>, List<APathLookup<*>>> = { path ->
            val crumbs = path.toCrumbs()

            if (holderToken == null) holderToken = localGateway.keepAliveHolder.get()
            val listing = localGateway.lookupFiles(path, mode = mode)
                    .sortedBy { it.name.toLowerCase(Locale.ROOT) }
                    .filter { !options.onlyDirs || it.isDirectory }

            Triple(path, crumbs, listing)
        }

        Observable.just(unwrapped)
                .map { path -> doCd(path) }
                .onErrorReturn {
                    errorEvents.postValue(it)
                    doCd(fallbackPath)
                }
                .onErrorReturn {
                    errorEvents.postValue(it)
                    Triple(fallbackPath, fallbackPath.toCrumbs(), emptyList())
                }
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

    fun onPermissionResult(granted: Boolean) {
        if (!granted) return

        selectItem(options.startPath)
    }

    fun grantPermission() {
        requestPermissionEvent.postValue(Any())
    }

    data class State(
            val currentPath: APath,
            val currentCrumbs: List<APath>,
            val currentListing: List<APathLookup<*>>? = null,
            val allowCreateDir: Boolean = false
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<LocalPickerFragmentVDC> {
        fun create(handle: SavedStateHandle, options: APathPicker.Options): LocalPickerFragmentVDC
    }

    companion object {
        val TAG = App.logTag("Picker", "Local", "VDC")
        const val ARG_MODE = "picker.local.mode"
    }
}