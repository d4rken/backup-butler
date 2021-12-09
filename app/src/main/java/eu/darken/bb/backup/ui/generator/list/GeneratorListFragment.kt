package eu.darken.bb.backup.ui.generator.list

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.backup.ui.generator.editor.GeneratorEditorResultListener
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.lists.differ.update
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.Smart2Fragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.GeneratorListFragmentBinding
import javax.inject.Inject

@AndroidEntryPoint
class GeneratorListFragment : Smart2Fragment(R.layout.generator_list_fragment), GeneratorEditorResultListener {

    override val vdc: GeneratorListFragmentVDC by viewModels()
    override val ui: GeneratorListFragmentBinding by viewBinding()
    @Inject lateinit var adapter: GeneratorListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupGeneratorEditorListener {
            log(TAG) { "setupGeneratorEditorListener(): $it" }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.generatorList.setupDefaults(adapter)

        vdc.viewState.observe2(this, ui) {
            log { "Updating UI state with $it" }
            adapter.update(it.generators)
            fab.isInvisible = false
        }

        ui.fab.setOnClickListener { vdc.newGenerator() }

        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        val TAG = logTag("Generator", "List", "Fragment")
    }
}
