package eu.darken.bb.common.file.picker.local

import android.os.Environment
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.common.Stater
import eu.darken.bb.common.file.APath
import eu.darken.bb.common.file.LocalPath
import eu.darken.bb.common.file.asFile
import eu.darken.bb.common.file.picker.APathPicker
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory

class LocalPickerFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val options: APathPicker.Options,
        private val builder: GeneratorBuilder
) : SmartVDC() {

    private val stater = Stater {
        val fallbackPath = LocalPath.build(Environment.getExternalStorageDirectory())
        val path: LocalPath = options.startPath as? LocalPath ?: fallbackPath
        val listing = path.asFile().listFiles().map { LocalPath.build(it) }
        val crumbs = mutableListOf<APath>()
        var jPath = path.asFile()
        while (jPath.parent != null) {
            crumbs.add(0, LocalPath.build(jPath.parentFile))
            jPath = jPath.parentFile
        }
        State(
                currentPath = path,
                currentListing = listing,
                currentCrumbs = crumbs
        )
    }
    val state = stater.liveData

    fun selectItem(path: APath) {
        TODO("not implemented")
    }

    data class State(
            val currentPath: APath,
            val currentListing: List<APath>,
            val currentCrumbs: List<APath>
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<LocalPickerFragmentVDC> {
        fun create(handle: SavedStateHandle, options: APathPicker.Options): LocalPickerFragmentVDC
    }

    companion object {
        val TAG = App.logTag("Picker", "Local", "VDC")
    }
}