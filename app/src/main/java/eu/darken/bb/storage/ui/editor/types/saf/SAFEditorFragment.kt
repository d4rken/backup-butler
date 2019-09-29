package eu.darken.bb.storage.ui.editor.types.saf

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import butterknife.BindView
import com.jakewharton.rxbinding3.widget.editorActions
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.observe2
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.setTextIfDifferentAndNotFocused
import eu.darken.bb.common.ui.BaseEditorFragment
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.common.userTextChangeEvents
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import eu.darken.bb.storage.core.getStorageId
import eu.darken.bb.storage.ui.list.StorageAdapter
import javax.inject.Inject


class SAFEditorFragment : BaseEditorFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    override val vdc: SAFEditorFragmentVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as SAFEditorFragmentVDC.Factory
        factory.create(handle, arguments!!.getStorageId()!!)
    })

    @Inject lateinit var adapter: StorageAdapter


    @BindView(R.id.name_input) lateinit var labelInput: EditText


    @BindView(R.id.path_display) lateinit var pathDisplay: TextView
    @BindView(R.id.path_button) lateinit var pathSelect: TextView


    @BindView(R.id.core_settings_container) lateinit var coreSettingsContainer: ViewGroup
    @BindView(R.id.core_settings_progress) lateinit var coreSettingsProgress: View

    @BindView(R.id.permission_card) lateinit var permissionCard: View
    @BindView(R.id.permission_grant_button) lateinit var permissionGrant: Button

    init {
        layoutRes = R.layout.storage_editor_saf_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivityActionBar().subtitle = getString(R.string.repo_type_saf_storage_label)

        vdc.state.observe(this, Observer { state ->
            labelInput.setTextIfDifferentAndNotFocused(state.label)

            pathDisplay.text = state.path

            coreSettingsContainer.setInvisible(state.isWorking)
            coreSettingsProgress.setInvisible(!state.isWorking)
            permissionCard.setGone(state.isPermissionGranted)
        })

        pathSelect.clicksDebounced().subscribe { vdc.selectPath() }

        labelInput.userTextChangeEvents().subscribe { vdc.updateName(it.text.toString()) }
        labelInput.editorActions { it == KeyEvent.KEYCODE_ENTER }.subscribe { labelInput.clearFocus() }

        vdc.openPickerEvent.observe2(this) {
            startActivityForResult(it, 13)
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) {
        if (requestCode == 13) {
            if (resultCode == Activity.RESULT_OK && result?.data != null) {
                vdc.onPermissionResult(result.data!!)
            } else {
                Toast.makeText(context, R.string.msg_please_try_again, Toast.LENGTH_SHORT).show()
            }
        } else {
            throw IllegalArgumentException("Unknown activity result: code=$requestCode, resultCode=$resultCode, result=$result")
        }
        super.onActivityResult(requestCode, resultCode, result)
    }

}
