package eu.darken.bb.backup.ui.generator.editor.types.files

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.backup.ui.generator.editor.GeneratorEditorFragmentChild
import eu.darken.bb.backup.ui.generator.editor.setGeneratorEditorResult
import eu.darken.bb.common.error.asErrorDialogBuilder
import eu.darken.bb.common.files.ui.picker.PathPickerActivityContract
import eu.darken.bb.common.navigation.popBackStack
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.setTextIfDifferentAndNotFocused
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.userTextChangeEvents
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.GeneratorEditorFileFragmentBinding

@AndroidEntryPoint
class FilesEditorConfigFragment : SmartFragment(R.layout.generator_editor_file_fragment),
    GeneratorEditorFragmentChild {

    val navArgs by navArgs<FilesEditorConfigFragmentArgs>()

    private val vdc: FilesEditorConfigFragmentVDC by viewModels()
    private val ui: GeneratorEditorFileFragmentBinding by viewBinding()

    init {
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.apply {
            toolbar.apply {
                setNavigationOnClickListener { popBackStack() }
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_create -> {
                            vdc.saveConfig()
                            true
                        }
                        else -> super.onOptionsItemSelected(it)
                    }
                }
            }
            nameInput.userTextChangeEvents().subscribe { vdc.updateLabel(it.text.toString()) }
            pathSelectAction.clicksDebounced().subscribe { vdc.showPicker() }
        }

        vdc.state.observe2(this, ui) { state ->
            nameInput.setTextIfDifferentAndNotFocused(state.label)
            pathDisplay.text = state.path?.userReadablePath(requireContext())

            pathSelectAction.setText(if (state.path == null) R.string.general_select_action else R.string.general_change_action)

            coreSettingsContainer.setInvisible(state.isWorking)
            coreSettingsLoadingOverlay.setInvisible(!state.isWorking)

            toolbar.menu.apply {
                findItem(R.id.action_create).title = getString(
                    if (state.isExisting) R.string.general_save_action else R.string.general_create_action
                )
                findItem(R.id.action_create).isVisible = state.isValid
            }
        }

        val pickerLauncher = registerForActivityResult(PathPickerActivityContract()) {
            if (it != null) vdc.updatePath(it)
            else Toast.makeText(requireContext(), R.string.general_error_empty_result_msg, Toast.LENGTH_SHORT).show()
        }
        vdc.pickerEvent.observe2(this) { pickerLauncher.launch(it) }

        vdc.finishEvent.observe2(this) {
            setGeneratorEditorResult(it)
            popBackStack()
        }

        vdc.errorEvent.observe2(this) { it.asErrorDialogBuilder(requireContext()).show() }

        super.onViewCreated(view, savedInstanceState)
    }

}
