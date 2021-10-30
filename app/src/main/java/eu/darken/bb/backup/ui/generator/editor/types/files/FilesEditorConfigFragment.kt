package eu.darken.bb.backup.ui.generator.editor.types.files

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.errors.asErrorDialogBuilder
import eu.darken.bb.common.files.ui.picker.PathPickerActivityContract
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.setTextIfDifferentAndNotFocused
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.userTextChangeEvents
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.GeneratorEditorFileFragmentBinding

@AndroidEntryPoint
class FilesEditorConfigFragment : SmartFragment(R.layout.generator_editor_file_fragment) {

    val navArgs by navArgs<FilesEditorConfigFragmentArgs>()

    private val vdc: FilesEditorConfigFragmentVDC by viewModels()
    private val ui: GeneratorEditorFileFragmentBinding by viewBinding()

    private var allowCreate: Boolean = false
    private var existing: Boolean = false

    init {
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.state.observe2(this) { state ->
            ui.nameInput.setTextIfDifferentAndNotFocused(state.label)
            ui.pathDisplay.text = state.path?.userReadablePath(requireContext())

            ui.pathSelectAction.setText(if (state.path == null) R.string.general_select_action else R.string.general_change_action)

            ui.coreSettingsContainer.setInvisible(state.isWorking)
            ui.coreSettingsLoadingOverlay.setInvisible(!state.isWorking)

            allowCreate = state.isValid
            existing = state.isExisting
            invalidateOptionsMenu()
        }

        ui.nameInput.userTextChangeEvents().subscribe { vdc.updateLabel(it.text.toString()) }
        ui.pathSelectAction.clicksDebounced().subscribe { vdc.showPicker() }

        val pickerLauncher = registerForActivityResult(PathPickerActivityContract()) {
            if (it != null) vdc.updatePath(it)
            else Toast.makeText(requireContext(), R.string.general_error_empty_result_msg, Toast.LENGTH_SHORT).show()
        }
        vdc.pickerEvent.observe2(this) { pickerLauncher.launch(it) }

        vdc.finishEvent.observe2(this) { requireActivity().finish() }

        vdc.errorEvent.observe2(this) { it.asErrorDialogBuilder(requireContext()).show() }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_generator_editor_files_config, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_create).isVisible = allowCreate
        menu.findItem(R.id.action_create).title =
            getString(if (existing) R.string.general_save_action else R.string.general_create_action)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_create -> {
            vdc.saveConfig()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

}
