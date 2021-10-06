package eu.darken.bb.storage.ui.editor.types.local

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding4.widget.editorActions
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.*
import eu.darken.bb.common.files.ui.picker.APathPicker
import eu.darken.bb.common.permission.Permission
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.databinding.StorageEditorLocalFragmentBinding
import eu.darken.bb.storage.core.ExistingStorageException

@AndroidEntryPoint
class LocalEditorFragment : SmartFragment(R.layout.storage_editor_local_fragment) {
    private val vdc: LocalEditorFragmentVDC by viewModels()
    private val ui: StorageEditorLocalFragmentBinding by viewBinding()

    private var allowCreate: Boolean = false
    private var existing: Boolean = false

    init {
        setHasOptionsMenu(true)
    }

    private lateinit var permissionWriteStorageLauncher: Permission.Launcher
    private lateinit var permissionManageStorageLauncher: Permission.Launcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionWriteStorageLauncher = Permission.WRITE_EXTERNAL_STORAGE.setup(this) { perm, _ ->
            vdc.onUpdatePermission(perm)
        }
        permissionManageStorageLauncher = Permission.MANAGE_EXTERNAL_STORAGE.setup(this) { perm, _ ->
            vdc.onUpdatePermission(perm)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.state.observe2(this) { state ->
            ui.nameInput.setTextIfDifferent(state.label)

            ui.pathDisplay.text = state.path
            ui.pathButton.isEnabled = !state.isExisting

            ui.coreSettingsContainer.setInvisible(state.isWorking)
            ui.coreSettingsProgress.setInvisible(!state.isWorking)
            ui.writeStorageCard.isGone = !state.missingPermissions.contains(Permission.WRITE_EXTERNAL_STORAGE)
            ui.manageStorageCard.isGone = !state.missingPermissions.contains(Permission.MANAGE_EXTERNAL_STORAGE)

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

        ui.writeStorageGrant.clicksDebounced().subscribe { vdc.requestPermission(Permission.WRITE_EXTERNAL_STORAGE) }
        ui.manageStorageGrant.clicksDebounced().subscribe { vdc.requestPermission(Permission.MANAGE_EXTERNAL_STORAGE) }

        vdc.requestPermissionEvent.observe2(this) { perm ->
            when (perm) {
                Permission.WRITE_EXTERNAL_STORAGE -> permissionWriteStorageLauncher.launch()
                Permission.MANAGE_EXTERNAL_STORAGE -> permissionManageStorageLauncher.launch()
                else -> throw UnsupportedOperationException("Requesting $perm is not setup for this screen.")
            }
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
