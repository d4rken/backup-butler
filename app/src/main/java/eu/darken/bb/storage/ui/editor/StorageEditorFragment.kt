package eu.darken.bb.storage.ui.editor

import androidx.fragment.app.viewModels
import androidx.viewbinding.ViewBinding
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.smart.Smart2Fragment

/**
 * Start point for storage creation/edit
 * Does the routing
 */
@AndroidEntryPoint
class StorageEditorFragment : Smart2Fragment(R.layout.storage_editor_fragment) {

    override val ui: ViewBinding? = null
    override val vdc: StorageEditorFragmentVDC by viewModels()

}
