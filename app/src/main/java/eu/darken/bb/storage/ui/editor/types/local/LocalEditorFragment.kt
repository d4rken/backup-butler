package eu.darken.bb.storage.ui.editor.types.local

import android.Manifest
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.Observer
import butterknife.BindView
import com.jakewharton.rxbinding3.widget.editorActions
import eu.darken.bb.R
import eu.darken.bb.common.*
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.ui.BaseEditorFragment
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.storage.core.getStorageId
import eu.darken.bb.storage.ui.list.StorageAdapter
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class LocalEditorFragment : BaseEditorFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    override val vdc: LocalEditorFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as LocalEditorFragmentVDC.Factory
        factory.create(handle, arguments!!.getStorageId()!!)
    })

    @Inject lateinit var adapter: StorageAdapter

    @BindView(R.id.path_input) lateinit var pathInput: EditText
    @BindView(R.id.path_input_layout) lateinit var pathInputLayout: ViewGroup
    @BindView(R.id.name_input) lateinit var labelInput: EditText
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
            pathInput.setTextIfDifferent(state.path)
            pathInput.setSelection(pathInput.text.length)

            labelInput.setTextIfDifferent(state.label)
            labelInput.setSelection(labelInput.text.length)

            if (state.path.isNotEmpty()) {
                pathInputLayout.isEnabled = !state.isExisting
                pathInput.setTextColor(getColorForAttr(if (state.validPath || state.isExisting) R.attr.colorOnBackground else R.attr.colorError))
            }

            coreSettingsContainer.setInvisible(state.isWorking)
            coreSettingsProgress.setInvisible(!state.isWorking)
            permissionCard.setGone(state.isPermissionGranted)
        })

        pathInput.userTextChangeEvents().debounce(2, TimeUnit.SECONDS).subscribe { vdc.updatePath(it.text.toString()) }
        pathInput.editorActions { it == KeyEvent.KEYCODE_ENTER }.subscribe { pathInput.clearFocus() }
        labelInput.userTextChangeEvents().debounce(2, TimeUnit.SECONDS).subscribe { vdc.updateName(it.text.toString()) }
        labelInput.editorActions { it == KeyEvent.KEYCODE_ENTER }.subscribe { labelInput.clearFocus() }

        permissionGrant.clicksDebounced().subscribe { vdc.onGrantPermission() }

        vdc.requestPermissionEvent.observe2(this) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1) {
            vdc.onPermissionResult()
        } else {
            throw IllegalArgumentException("Unknown permission request: code=$requestCode, permissions=$permissions")
        }
    }

}
