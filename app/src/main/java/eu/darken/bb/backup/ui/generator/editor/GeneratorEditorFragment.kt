package eu.darken.bb.backup.ui.generator.editor

import android.os.Bundle
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.smart.SmartFragment

/**
 * Start point for generator creation/edit
 * Does the routing
 */
@AndroidEntryPoint
class GeneratorEditorFragment : SmartFragment(R.layout.generator_editor_fragment) {

    val vdc: GeneratorEditorFragmentVDC by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vdc.navEvents.observe(this) { doNavigate(it) }
    }

}
