package eu.darken.bb.storage.ui.editor

import android.os.Bundle
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.storage.ui.list.StorageAdapter
import javax.inject.Inject

/**
 * Start point for storage creation/edit
 * Does the routing
 */
@AndroidEntryPoint
class StorageEditorFragment : SmartFragment(R.layout.storage_editor_fragment) {

    private val vdc: StorageEditorFragmentVDC by viewModels()

    @Inject lateinit var adapter: StorageAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vdc.navEvents.observe(this) { doNavigate(it) }
    }
}
