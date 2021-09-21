package eu.darken.bb.storage.ui.editor.types.local

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding4.widget.editorActions
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.*
import eu.darken.bb.common.files.ui.picker.APathPicker
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.databinding.StorageEditorLocalFragmentBinding
import eu.darken.bb.storage.core.ExistingStorageException
import eu.darken.bb.storage.ui.list.StorageAdapter
import javax.inject.Inject

@AndroidEntryPoint
class LocalEditorFragment : SmartFragment(R.layout.storage_editor_local_fragment) {
    private val vdc: LocalEditorFragmentVDC by viewModels()
    private val ui: StorageEditorLocalFragmentBinding by viewBinding()
    @Inject lateinit var adapter: StorageAdapter

    private var allowCreate: Boolean = false
    private var existing: Boolean = false

    init {
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.state.observe2(this) { state ->
            ui.nameInput.setTextIfDifferent(state.label)

            ui.pathDisplay.text = state.path
            ui.pathButton.isEnabled = !state.isExisting

            ui.coreSettingsContainer.setInvisible(state.isWorking)
            ui.coreSettingsProgress.setInvisible(!state.isWorking)
            ui.permissionCard.setGone(state.isPermissionGranted)

            allowCreate = state.isValid
            existing = state.isExisting
            requireActivity().invalidateOptionsMenu()
        }

        ui.pathButton.clicksDebounced().subscribe { vdc.selectPath() }

        vdc.pickerEvent.observe2(this) {
            val intent = APathPicker.createIntent(requireContext(), it)
            startActivityForResult(intent, 47)
        }

        ui.nameInput.userTextChangeEvents().subscribe { vdc.updateName(it.text.toString()) }
        ui.nameInput.editorActions { it == KeyEvent.KEYCODE_ENTER }.subscribe { ui.nameInput.clearFocus() }

        vdc.errorEvent.observe2(this) { error ->
            val snackbar = Snackbar.make(
                view,
                error.tryLocalizedErrorMessage(requireContext()),
                Snackbar.LENGTH_LONG
            )
            if (error is ExistingStorageException) {
                snackbar.setAction(R.string.general_import_action) {
                    vdc.importStorage(error.path)
                }
            }
            snackbar.show()
        }

        ui.permissionGrantButton.clicksDebounced().subscribe { vdc.onGrantPermission() }

        vdc.requestPermissionEvent.observe2(this) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        vdc.finishEvent.observe2(this) {
            finishActivity()
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            47 -> APathPicker.checkForNonNeutralResult(this, resultCode, data) { vdc.updatePath(it) }
            else -> throw IllegalArgumentException("Unknown activity result: code=$requestCode, resultCode=$resultCode, data=$data")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1) {
            vdc.onPermissionResult()
        } else {
            throw IllegalArgumentException("Unknown permission request: code=$requestCode, permissions=$permissions")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_storage_editor_local_fragment, menu)
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
