package eu.darken.bb.backup.ui.generator.editor

import androidx.fragment.app.viewModels
import androidx.viewbinding.ViewBinding
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.smart.Smart2Fragment

/**
 * Start point for generator creation/edit
 * Does the routing
 */
@AndroidEntryPoint
class GeneratorEditorFragment : Smart2Fragment(R.layout.generator_editor_fragment) {

    override val vdc: GeneratorEditorFragmentVDC by viewModels()
    override val ui: ViewBinding? = null

}
