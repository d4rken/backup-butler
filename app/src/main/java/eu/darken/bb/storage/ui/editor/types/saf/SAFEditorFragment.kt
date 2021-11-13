package eu.darken.bb.storage.ui.editor.types.saf

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding4.widget.editorActions
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.error.localized
import eu.darken.bb.common.files.ui.picker.PathPickerActivityContract
import eu.darken.bb.common.navigation.popBackStack
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.setTextIfDifferent
import eu.darken.bb.common.smart.Smart2Fragment
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.userTextChangeEvents
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.StorageEditorSafFragmentBinding
import eu.darken.bb.storage.core.ExistingStorageException
import eu.darken.bb.storage.ui.editor.StorageEditorFragmentChild
import eu.darken.bb.storage.ui.editor.setStorageEditorResult
import eu.darken.bb.storage.ui.list.StorageAdapter
import javax.inject.Inject

@AndroidEntryPoint
class SAFEditorFragment : Smart2Fragment(R.layout.storage_editor_saf_fragment), StorageEditorFragmentChild {

    override val vdc: SAFEditorFragmentVDC by viewModels()
    override val ui: StorageEditorSafFragmentBinding by viewBinding()
    @Inject lateinit var adapter: StorageAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.toolbar.apply {
            setupWithNavController(findNavController())
            setNavigationIcon(R.drawable.ic_baseline_close_24)
            inflateMenu(R.menu.menu_storage_editor_saf_fragment)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_create -> {
                        vdc.saveConfig()
                        true
                    }
                    else -> super.onOptionsItemSelected(item)
                }
            }
        }

        vdc.state.observe2(this, ui) { state ->
            nameInput.setTextIfDifferent(state.label)

            pathDisplay.text = state.path
            pathButton.isEnabled = !state.isExisting

            coreSettingsContainer.setInvisible(state.isWorking)
            coreSettingsProgress.setInvisible(!state.isWorking)

            toolbar.apply {
                menu.findItem(R.id.action_create).isVisible = state.isValid
                menu.findItem(R.id.action_create).title = getString(
                    if (state.isExisting) R.string.general_save_action else R.string.general_create_action
                )
                setTitle(if (state.isExisting) R.string.storage_edit_action else R.string.storage_create_action)
            }
        }

        ui.pathButton.clicksDebounced().subscribe { vdc.selectPath() }

        ui.nameInput.userTextChangeEvents().subscribe { vdc.updateName(it.text.toString()) }
        ui.nameInput.editorActions { it == KeyEvent.KEYCODE_ENTER }.subscribe { ui.nameInput.clearFocus() }

        val pickerLauncher = registerForActivityResult(PathPickerActivityContract()) {
            if (it != null) vdc.onUpdatePath(it)
            else Toast.makeText(requireContext(), R.string.general_error_empty_result_msg, Toast.LENGTH_SHORT).show()
        }
        vdc.openPickerEvent.observe2(this) { pickerLauncher.launch(it) }

        onErrorEvent = { error ->
            if (error is ExistingStorageException) {
                val snackbar = Snackbar.make(
                    view,
                    error.localized(requireContext()).asText(),
                    Snackbar.LENGTH_LONG
                )
                snackbar.setAction(R.string.general_import_action) {
                    vdc.importStorage(error.path)
                }
                snackbar.show()
                false
            } else {
                true
            }
        }

        vdc.finishEvent.observe2(this) {
            setStorageEditorResult(it)
            popBackStack()
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
