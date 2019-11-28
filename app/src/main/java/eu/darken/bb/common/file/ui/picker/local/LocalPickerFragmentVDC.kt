package eu.darken.bb.common.file.ui.picker.local

import android.Manifest
import android.os.Environment
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.common.RuntimePermissionTool
import eu.darken.bb.common.SharedResource
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.file.core.APath
import eu.darken.bb.common.file.core.APathLookup
import eu.darken.bb.common.file.core.WriteException
import eu.darken.bb.common.file.core.local.LocalGateway
import eu.darken.bb.common.file.core.local.LocalPath
import eu.darken.bb.common.file.core.local.LocalPathLookup
import eu.darken.bb.common.file.core.local.toCrumbs
import eu.darken.bb.common.file.ui.picker.APathPicker
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.IOException
import java.util.*

class LocalPickerFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val options: APathPicker.Options,
        private val localGateway: LocalGateway,
        private val permissionTool: RuntimePermissionTool
) : SmartVDC() {

    private val startPath = options.startPath as? LocalPath
    private val mode: LocalGateway.Mode by lazy {
        options.payload.classLoader = App::class.java.classLoader
        LocalGateway.Mode.valueOf(options.payload.getString(ARG_MODE, LocalGateway.Mode.AUTO.name))
    }

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
    val missingPermissionEvent = SingleLiveEvent<Any>()
    val requestPermissionEvent = SingleLiveEvent<Any>()
    var resourceToken: SharedResource.Resource<*>? = null


    init {
        if (!permissionTool.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            missingPermissionEvent.postValue(Any())
        }
    }

    override fun onCleared() {
        resourceToken?.close()
        super.onCleared()
    }

    private fun doCd(_path: LocalPath?): Triple<LocalPath, List<LocalPath>, List<APathLookup<*>>> {
        val path = _path ?: LocalPath.build(Environment.getExternalStorageDirectory())
        val crumbs = path.toCrumbs()

        val listing = try {
            if (resourceToken == null) resourceToken = localGateway.resourceTokens.get()
            localGateway.lookupFiles(path, mode = mode)
                    .sortedBy { it.name.toLowerCase(Locale.ROOT) }
                    .filter { !options.onlyDirs || it.isDirectory }
        } catch (e: IOException) {
            Timber.tag(TAG).e(e, "Failed to cd %s", path)
            errorEvents.postValue(e)
            emptyList<APathLookup<*>>()
        }

        return Triple(path, crumbs, listing)
    }

    fun selectItem(selected: APath?) {
        val unwrapped = when (selected) {
            is LocalPath -> selected
            is LocalPathLookup -> selected.lookedUp
            else -> null
        }
        Observable
                .fromCallable { doCd(unwrapped) }
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

        Observable
                .fromCallable { doCd(options.startPath as? LocalPath) }
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

    fun grantPermission() {
        requestPermissionEvent.postValue(Any())
    }

    data class State(
            val currentPath: APath,
            val currentCrumbs: List<APath>,
            val currentListing: List<APathLookup<*>>,
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