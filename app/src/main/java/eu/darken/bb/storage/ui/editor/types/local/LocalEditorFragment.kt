package eu.darken.bb.storage.ui.editor.types.local

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding4.widget.editorActions
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.errors.localized
import eu.darken.bb.common.files.ui.picker.PathPickerActivityContract
import eu.darken.bb.common.navigation.popBackStack
import eu.darken.bb.common.observe2
import eu.darken.bb.common.permission.Permission
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.setTextIfDifferent
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.userTextChangeEvents
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.StorageEditorLocalFragmentBinding
import eu.darken.bb.storage.core.ExistingStorageException
import eu.darken.bb.storage.ui.editor.StorageEditorFragmentChild
import eu.darken.bb.storage.ui.editor.setStorageEditorResult

@AndroidEntryPoint
class LocalEditorFragment : SmartFragment(R.layout.storage_editor_local_fragment), StorageEditorFragmentChild {
    private val vdc: LocalEditorFragmentVDC by viewModels()
    private val ui: StorageEditorLocalFragmentBinding by viewBinding()


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
        ui.toolbar.apply {
            setupWithNavController(findNavController())
            inflateMenu(R.menu.menu_storage_editor_local_fragment)
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
            ui.nameInput.setTextIfDifferent(state.label)

            ui.pathDisplay.text = state.path
            ui.pathButton.isEnabled = !state.isExisting

            ui.coreSettingsContainer.setInvisible(state.isWorking)
            ui.coreSettingsProgress.setInvisible(!state.isWorking)
            ui.writeStorageCard.isGone = !state.missingPermissions.contains(Permission.WRITE_EXTERNAL_STORAGE)
            ui.manageStorageCard.isGone = !state.missingPermissions.contains(Permission.MANAGE_EXTERNAL_STORAGE)

            toolbar.apply {
                menu.findItem(R.id.action_create).isVisible = state.isValid
                menu.findItem(R.id.action_create).title = getString(
                    if (state.isExisting) R.string.general_save_action else R.string.general_create_action
                )
                setTitle(if (state.isExisting) R.string.storage_edit_action else R.string.storage_create_action)
            }
        }

        ui.pathButton.clicksDebounced().subscribe { vdc.selectPath() }

        val pickerLauncher = registerForActivityResult(PathPickerActivityContract()) {
            if (it != null) vdc.updatePath(it)
            else Toast.makeText(requireContext(), R.string.general_error_empty_result_msg, Toast.LENGTH_SHORT).show()
        }
        vdc.pickerEvent.observe2(this) { pickerLauncher.launch(it) }

        ui.nameInput.userTextChangeEvents().subscribe { vdc.updateName(it.text.toString()) }
        ui.nameInput.editorActions { it == KeyEvent.KEYCODE_ENTER }.subscribe { ui.nameInput.clearFocus() }

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
            setStorageEditorResult(it)
            popBackStack()
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
