package eu.darken.bb.storage.ui.editor.types.local

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.navigation.fragment.navArgs
import butterknife.BindView
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.widget.editorActions
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.files.ui.picker.APathPicker
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.setTextIfDifferent
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.tryLocalizedErrorMessage
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.userTextChangeEvents
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.storage.core.ExistingStorageException
import eu.darken.bb.storage.ui.list.StorageAdapter
import javax.inject.Inject


class LocalEditorFragment : SmartFragment(), AutoInject {

    val navArgs by navArgs<LocalEditorFragmentArgs>()

    @Inject lateinit var vdcSource: VDCSource.Factory
    val vdc: LocalEditorFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as LocalEditorFragmentVDC.Factory
        factory.create(handle, navArgs.storageId)
    })

    @Inject lateinit var adapter: StorageAdapter

    @BindView(R.id.name_input) lateinit var labelInput: EditText

    @BindView(R.id.path_display) lateinit var pathDisplay: TextView
    @BindView(R.id.path_button) lateinit var pathSelect: TextView

    @BindView(R.id.core_settings_container) lateinit var coreSettingsContainer: ViewGroup
    @BindView(R.id.core_settings_progress) lateinit var coreSettingsProgress: View

    @BindView(R.id.permission_card) lateinit var permissionCard: View
    @BindView(R.id.permission_grant_button) lateinit var permissionGrant: Button

    private var allowCreate: Boolean = false
    private var existing: Boolean = false

    init {
        layoutRes = R.layout.storage_editor_local_fragment
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.state.observe2(this) { state ->
            labelInput.setTextIfDifferent(state.label)

            pathDisplay.text = state.path
            pathSelect.isEnabled = !state.isExisting

            coreSettingsContainer.setInvisible(state.isWorking)
            coreSettingsProgress.setInvisible(!state.isWorking)
            permissionCard.setGone(state.isPermissionGranted)

            allowCreate = state.isValid
            existing = state.isExisting
            requireActivity().invalidateOptionsMenu()
        }

        pathSelect.clicksDebounced().subscribe { vdc.selectPath() }

        vdc.pickerEvent.observe2(this) {
            val intent = APathPicker.createIntent(requireContext(), it)
            startActivityForResult(intent, 47)
        }

        labelInput.userTextChangeEvents().subscribe { vdc.updateName(it.text.toString()) }
        labelInput.editorActions { it == KeyEvent.KEYCODE_ENTER }.subscribe { labelInput.clearFocus() }

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

        permissionGrant.clicksDebounced().subscribe { vdc.onGrantPermission() }

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
        menu.findItem(R.id.action_create).title = getString(if (existing) R.string.general_save_action else R.string.general_create_action)
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
