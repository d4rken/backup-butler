package eu.darken.bb.backup.ui.generator.list

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.backup.ui.generator.editor.GeneratorEditorResultListener
import eu.darken.bb.common.debug.BBDebug
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.lists.differ.update
import eu.darken.bb.common.lists.setupDefaults
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
        ui.apply {
            generatorList.setupDefaults(adapter)
            fab.setOnClickListener { vdc.newGenerator() }
        }

        vdc.viewState.observe2(ui) {
            log { "Updating UI state with $it" }
            adapter.update(it.generators)
            fab.isInvisible = false
        }

        vdc.showSingleUseExplanation.observe2 { generatorId ->
            val builder = MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.singleuse_item_label)
                .setMessage(R.string.singleuse_item_description)
                .setPositiveButton(R.string.general_ok_action) { _, _ -> }
            if (BBDebug.isDebug()) {
                builder.setNeutralButton(R.string.general_edit_action) { _, _ ->
                    vdc.editGenerator(generatorId)
                }
            }
            builder.show()
        }

        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        val TAG = logTag("Generator", "List", "Fragment")
    }
}
