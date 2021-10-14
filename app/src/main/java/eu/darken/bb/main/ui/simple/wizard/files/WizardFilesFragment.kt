package eu.darken.bb.main.ui.simple.wizard.files

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.errors.asErrorDialogBuilder
import eu.darken.bb.common.lists.modular.mods.TypedVHCreatorMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.navigation.popBackStack
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.SimpleModeWizardFilesFragmentBinding
import eu.darken.bb.main.ui.simple.wizard.common.WizardAdapter

@AndroidEntryPoint
class WizardFilesFragment : SmartFragment(R.layout.simple_mode_wizard_files_fragment) {

    private val vdc: WizardFilesFragmentVDC by viewModels()
    private val ui: SimpleModeWizardFilesFragmentBinding by viewBinding()

    private val adapter = WizardAdapter { data ->
        listOf(
            TypedVHCreatorMod({ data[it] is FilesPathInfoVH.Item }) { FilesPathInfoVH(it) },
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.apply {
            recyclerView.setupDefaults(adapter, dividers = false)
            toolbar.apply {
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_save -> {
                            vdc.onSave()
                            true
                        }
                        else -> false
                    }
                }
                setNavigationOnClickListener { popBackStack() }
            }
        }

        vdc.state.observe2(this, ui) { state ->
            toolbar.menu.findItem(R.id.action_save).apply {
                setTitle(if (state.isExisting) R.string.general_save_action else R.string.general_create_action)
                setIcon(if (state.isExisting) R.drawable.ic_baseline_save_24 else R.drawable.ic_add_task)
                isVisible = true
            }
            adapter.update(state.items)
        }

        vdc.finishEvent.observe2(this) { popBackStack() }
        vdc.errorEvent.observe2(this) { it.asErrorDialogBuilder(requireContext()).show() }

        super.onViewCreated(view, savedInstanceState)
    }
}
