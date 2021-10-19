package eu.darken.bb.quickmode.ui.wizard.files

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.errors.asErrorDialogBuilder
import eu.darken.bb.common.lists.modular.mods.TypedVHCreatorMod
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.navigation.popBackStack
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.QuickmodeWizardFilesFragmentBinding
import eu.darken.bb.quickmode.ui.wizard.common.WizardAdapter
import eu.darken.bb.storage.ui.picker.StoragePickerResultListener

@AndroidEntryPoint
class WizardFilesFragment : SmartFragment(R.layout.quickmode_wizard_files_fragment), StoragePickerResultListener {

    private val vdc: WizardFilesFragmentVDC by viewModels()
    private val ui: QuickmodeWizardFilesFragmentBinding by viewBinding()

    private val adapter = WizardAdapter { data ->
        listOf(
            TypedVHCreatorMod({ data[it] is FilesPathInfoVH.Item }) { FilesPathInfoVH(it) },
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupStoragePickerListener {
            vdc.onStoragePickerResult(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.apply {
            recyclerView.setupDefaults(adapter, dividers = false)
            toolbar.apply {
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_save -> {
                            vdc.saveTask()
                            true
                        }
                        R.id.action_remove -> {
                            vdc.removeTask()
                            true
                        }
                        else -> false
                    }
                }
                setNavigationOnClickListener { popBackStack() }
            }
        }

        vdc.state.observe2(this, ui) { state ->
            toolbar.menu.apply {
                findItem(R.id.action_save).apply {
                    setTitle(if (state.isExisting) R.string.general_save_action else R.string.general_create_action)
                    isVisible = true
                }
                findItem(R.id.action_remove).isVisible = state.isExisting
            }
            adapter.update(state.items)
        }

        vdc.navEvents.observe2(this) {
            it?.run { doNavigate(this) } ?: popBackStack()
        }

        vdc.errorEvent.observe2(this) { it.asErrorDialogBuilder(requireContext()).show() }

        super.onViewCreated(view, savedInstanceState)
    }
}
