package eu.darken.bb.storage.ui.editor.types.saf

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding4.widget.editorActions
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.errors.localized
import eu.darken.bb.common.files.ui.picker.APathPicker
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.setTextIfDifferent
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.userTextChangeEvents
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.StorageEditorSafFragmentBinding
import eu.darken.bb.storage.core.ExistingStorageException
import eu.darken.bb.storage.ui.list.StorageAdapter
import javax.inject.Inject

@AndroidEntryPoint
class SAFEditorFragment : SmartFragment(R.layout.storage_editor_saf_fragment) {

    private val vdc: SAFEditorFragmentVDC by viewModels()
    private val ui: StorageEditorSafFragmentBinding by viewBinding()
    @Inject lateinit var adapter: StorageAdapter

    private var allowCreate: Boolean = false
    private var existing: Boolean = false

    init {
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.state.observe2(this, ui) { state ->
            nameInput.setTextIfDifferent(state.label)

            pathDisplay.text = state.path
            pathButton.isEnabled = !state.isExisting

            coreSettingsContainer.setInvisible(state.isWorking)
            coreSettingsProgress.setInvisible(!state.isWorking)

            allowCreate = state.isValid
            existing = state.isExisting
            requireActivity().invalidateOptionsMenu()
        }

        ui.pathButton.clicksDebounced().subscribe { vdc.selectPath() }

        ui.nameInput.userTextChangeEvents().subscribe { vdc.updateName(it.text.toString()) }
        ui.nameInput.editorActions { it == KeyEvent.KEYCODE_ENTER }.subscribe { ui.nameInput.clearFocus() }

        vdc.openPickerEvent.observe2(this) {
            startActivityForResult(APathPicker.createIntent(requireContext(), it), 13)
        }

        vdc.errorEvent.observe2(this) { error ->
            val snackbar = Snackbar.make(
                view,
                error.localized(requireContext()).asText(),
                Snackbar.LENGTH_LONG
            )
            if (error is ExistingStorageException) {
                snackbar.setAction(R.string.general_import_action) {
                    vdc.importStorage(error.path)
                }
            }
            snackbar.show()
        }

        vdc.finishEvent.observe2(this) {
            finishActivity()
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            13 -> APathPicker.checkForNonNeutralResult(this, resultCode, data) { vdc.onUpdatePath(it) }
            else -> throw IllegalArgumentException("Unknown activity result: code=$requestCode, resultCode=$resultCode, data=$data")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_storage_editor_saf_fragment, menu)
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
