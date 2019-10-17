package eu.darken.bb.storage.ui.editor.types.local

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import butterknife.BindView
import com.jakewharton.rxbinding3.widget.editorActions
import eu.darken.bb.R
import eu.darken.bb.common.*
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.filepicker.FilePicker
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.ui.BaseEditorFragment
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.storage.core.ExistingStorageException
import eu.darken.bb.storage.core.getStorageId
import eu.darken.bb.storage.ui.list.StorageAdapter
import javax.inject.Inject


class LocalEditorFragment : BaseEditorFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    override val vdc: LocalEditorFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as LocalEditorFragmentVDC.Factory
        factory.create(handle, arguments!!.getStorageId()!!)
    })

    @Inject lateinit var adapter: StorageAdapter
    @Inject lateinit var filePicker: FilePicker

    @BindView(R.id.name_input) lateinit var labelInput: EditText

    @BindView(R.id.path_display) lateinit var pathDisplay: TextView
    @BindView(R.id.path_button) lateinit var pathSelect: TextView

    @BindView(R.id.core_settings_container) lateinit var coreSettingsContainer: ViewGroup
    @BindView(R.id.core_settings_progress) lateinit var coreSettingsProgress: View

    @BindView(R.id.permission_card) lateinit var permissionCard: View
    @BindView(R.id.permission_grant_button) lateinit var permissionGrant: Button

    init {
        layoutRes = R.layout.storage_editor_local_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().subtitle = getString(R.string.repo_type_local_storage_label)

        vdc.state.observe(this, Observer { state ->
            labelInput.setTextIfDifferent(state.label)

            pathDisplay.text = state.path
            pathSelect.isEnabled = !state.isExisting

            coreSettingsContainer.setInvisible(state.isWorking)
            coreSettingsProgress.setInvisible(!state.isWorking)
            permissionCard.setGone(state.isPermissionGranted)
        })

        pathSelect.clicksDebounced().subscribe { vdc.selectPath() }

        vdc.pickerEvent.observe2(this) {
            filePicker.launchPicker(this, it)
        }

        labelInput.userTextChangeEvents().subscribe { vdc.updateName(it.text.toString()) }
        labelInput.editorActions { it == KeyEvent.KEYCODE_ENTER }.subscribe { labelInput.clearFocus() }

        vdc.errorEvent.observe2(this) { error ->
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage(error.tryLocalizedErrorMessage(requireContext()))

            if (error is ExistingStorageException) {
                builder.setNeutralButton(R.string.action_cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                builder.setPositiveButton(R.string.action_import) { _, _ ->
                    vdc.importStorage(error.path)
                }
            }

            builder.show()
        }

        permissionGrant.clicksDebounced().subscribe { vdc.onGrantPermission() }

        vdc.requestPermissionEvent.observe2(this) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val path = filePicker.getResult(requestCode, resultCode, data).first()
        vdc.updatePath(path)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1) {
            vdc.onPermissionResult()
        } else {
            throw IllegalArgumentException("Unknown permission request: code=$requestCode, permissions=$permissions")
        }
    }

}
